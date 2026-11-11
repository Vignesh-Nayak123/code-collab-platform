// 100-client STOMP-over-WebSocket load harness (reconstruction of the lost
// resume-step5 harness). Workload and metrics match resume-step5 exactly:
//   - CLIENTS STOMP clients connect to http://localhost/ws (nginx -> backend1/backend2 RR)
//   - every client subscribes to /topic/room/<room>/code-change
//   - every client sends EDITS code changes (total CLIENTS*EDITS)
//   - expected client deliveries = CLIENTS*EDITS*CLIENTS (each client receives every edit)
// Latency method (verified against the single-pair probe measure.mjs):
//   sender stamps performance.timeOrigin+performance.now() immediately before
//   the SEND frame; receiver records recvAt at WebSocket onmessage entry;
//   latencyMs = recvAt - sentAt (same host/process clock).
// p95 = nearest-rank (ceil(0.95*n)th value, 1-based).
//
// Usage: node ws-100x20.mjs <roomCode> [outJsonPath]
import { writeFileSync } from 'node:fs';

const WS_URL = process.env.WS_URL || 'ws://localhost/ws/websocket';
const CLIENTS = Number(process.env.CLIENTS || 100);
const EDITS = Number(process.env.EDITS || 20);
const SETTLE_MS = Number(process.env.SETTLE_MS || 500);
const MAX_WAIT_MS = Number(process.env.MAX_WAIT_MS || 90000);

const perfNow = () => performance.timeOrigin + performance.now();
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

const roomCode = process.argv[2];
const outPath = process.argv[3];
if (!roomCode) {
  console.error('roomCode required');
  process.exit(2);
}

class StompClient {
  constructor(name, headers = {}) {
    this.name = name;
    this.extraHeaders = headers;
    this.ws = null;
    this.buffer = '';
    this.subscriptions = new Map();
    this.connectedPromise = null;
    this._resolveConnected = null;
    this._rejectConnected = null;
    this.closedUnexpectedly = false;
  }

  connect() {
    this.connectedPromise = new Promise((resolve, reject) => {
      this._resolveConnected = resolve;
      this._rejectConnected = reject;
    });
    this.ws = new WebSocket(WS_URL);
    this.ws.onopen = () => {
      let frame = 'CONNECT\naccept-version:1.2\nhost:localhost\nheart-beat:0,0\n';
      for (const [k, v] of Object.entries(this.extraHeaders)) frame += `${k}:${v}\n`;
      this.ws.send(frame + '\n\0');
    };
    this.ws.onerror = () => this._rejectConnected?.(new Error(`${this.name} ws error`));
    this.ws.onclose = () => {
      if (!this._resolved) {
        this._rejectConnected?.(new Error(`${this.name} closed before CONNECTED`));
      } else {
        this.closedUnexpectedly = true;
      }
    };
    this.ws.onmessage = (event) => {
      const recvAt = perfNow();
      this.buffer += typeof event.data === 'string' ? event.data : Buffer.from(event.data).toString('utf8');
      let idx;
      while ((idx = this.buffer.indexOf('\0')) >= 0) {
        const raw = this.buffer.slice(0, idx);
        this.buffer = this.buffer.slice(idx + 1);
        this._onFrame(raw, recvAt);
      }
    };
    return this.connectedPromise;
  }

  _onFrame(raw, recvAt) {
    const clean = raw.replace(/^\n+/, '').replace(/\r\n/g, '\n');
    const split = clean.indexOf('\n\n');
    const head = split >= 0 ? clean.slice(0, split) : clean;
    const body = split >= 0 ? clean.slice(split + 2) : '';
    const lines = head.split('\n');
    const command = lines.shift();
    const headers = {};
    for (const line of lines) {
      const i = line.indexOf(':');
      if (i > 0) headers[line.slice(0, i)] = line.slice(i + 1);
    }

    if (command === 'CONNECTED') {
      this._resolved = true;
      this._resolveConnected?.(this);
      return;
    }
    if (command === 'ERROR') {
      this.stompError = headers.message || 'STOMP ERROR';
      this._rejectConnected?.(new Error(`${this.name} STOMP ERROR ${headers.message || ''}`));
      return;
    }
    if (command === 'MESSAGE') {
      const sub = this.subscriptions.get(headers.subscription);
      sub?.({ recvAt, headers, body });
    }
  }

  subscribe(destination, onMessage, id = 'sub-0') {
    this.subscriptions.set(id, onMessage);
    this.ws.send(`SUBSCRIBE\nid:${id}\ndestination:${destination}\nack:auto\n\n\0`);
  }

  send(destination, body) {
    const bytes = Buffer.byteLength(body, 'utf8');
    const frame = `SEND\ndestination:${destination}\ncontent-type:application/json\ncontent-length:${bytes}\n\n${body}\0`;
    const sentAt = perfNow();
    this.ws.send(frame);
    return sentAt;
  }

  close() {
    try { this.ws?.close(); } catch {}
  }
}

const runId = `run-${Date.now()}`;
const editDestination = `/app/editor/${roomCode}/code-change`;
const topicDestination = `/topic/room/${roomCode}/code-change`;
const totalEdits = CLIENTS * EDITS;
const expectedDeliveries = CLIENTS * EDITS * CLIENTS;

// per-client state
const clients = [];
const editIds = [];
for (let i = 0; i < totalEdits; i++) editIds.push(`${runId}:e${i}`);
// bitset per client for dedup: Map editId -> bool via Set of index
const receivedSet = new Array(CLIENTS).fill(null).map(() => new Set());
const framesReceived = new Array(CLIENTS).fill(0);
const latencySamples = [];
// passive telemetry: delivery counts per 500ms bucket since run start
const timelineStart = Date.now();
const timeline = [0]; // index i = deliveries during [i*500, (i+1)*500) ms

let connectionErrors = 0;
let unexpectedCloses = 0;
let stompErrors = 0;
let sentCount = 0;
let firstSendAt = null;
let lastSendAt = null;
let lastReceiptAt = null;

const runStart = Date.now();

console.log(`Connecting ${CLIENTS} clients to ${WS_URL} (room ${roomCode}) ...`);
const connectStart = perfNow();
for (let i = 0; i < CLIENTS; i++) {
  clients.push(new StompClient(`c${i}`, { username: `load-${i}`, roomCode }));
}
const connectResults = await Promise.allSettled(clients.map((c) => c.connect()));
const connected = [];
for (let i = 0; i < CLIENTS; i++) {
  if (connectResults[i].status === 'fulfilled') connected.push(clients[i]);
  else connectionErrors++;
}
const connectionSetupMs = perfNow() - connectStart;
console.log(`Connected ${connected.length}/${CLIENTS} in ${connectionSetupMs.toFixed(2)}ms`);

if (connected.length !== CLIENTS) {
  console.error('NOT ALL CLIENTS CONNECTED - aborting to preserve workload');
  for (const c of clients) c.close();
  process.exit(1);
}

// everyone subscribes; frames on one WebSocket are ordered so SUBSCRIBE lands
// before any SEND we make afterwards; settle to be safe.
for (let i = 0; i < CLIENTS; i++) {
  const idx = i;
  connected[i].subscribe(topicDestination, ({ recvAt, body }) => {
    framesReceived[idx]++;
    const bucket = Math.floor((Date.now() - timelineStart) / 500);
    while (timeline.length <= bucket) timeline.push(0);
    timeline[bucket]++;
    if (lastReceiptAt === null || recvAt > lastReceiptAt) lastReceiptAt = recvAt;
    let msg;
    try { msg = JSON.parse(body); } catch { return; }
    if (typeof msg.content !== 'string' || !msg.content.startsWith(`${runId}:`)) return;
    const editIdx = Number(msg.content.slice(`${runId}:e`.length));
    if (!Number.isInteger(editIdx)) return;
    const set = receivedSet[idx];
    if (set.has(editIdx)) return; // duplicate
    set.add(editIdx);
    const sentAt = Number(msg.timestamp);
    if (Number.isFinite(sentAt)) latencySamples.push(recvAt - sentAt);
  });
}
await sleep(SETTLE_MS);

// send phase: stagger client starts by 2ms to approximate the original
// ~279ms send window for 2000 edits; each client fires its EDITS edits back-to-back.
console.log(`Sending ${totalEdits} edits (${CLIENTS} clients x ${EDITS}) ...`);
const sendPhase = (async () => {
  for (let i = 0; i < CLIENTS; i++) {
    const c = connected[i];
    setTimeout(() => {
      for (let e = 0; e < EDITS; e++) {
        const editIdx = i * EDITS + e;
        const body = JSON.stringify({
          roomCode,
          username: `load-${i}`,
          content: editIds[editIdx],
          version: e + 1,
          timestamp: String(perfNow()),
        });
        const at = c.send(editDestination, body);
        if (firstSendAt === null) firstSendAt = at;
        lastSendAt = at;
        sentCount++;
      }
    }, i * 2);
  }
})();
// wait for all deliveries (or timeout)
const deadline = Date.now() + MAX_WAIT_MS;
let progressLogged = 0;
while (Date.now() < deadline) {
  const unique = receivedSet.reduce((s, set) => s + set.size, 0);
  if (unique >= expectedDeliveries) break;
  const pct = Math.floor((unique / expectedDeliveries) * 100);
  if (pct >= progressLogged + 10) {
    progressLogged = pct;
    console.log(`  deliveries: ${unique}/${expectedDeliveries} (${pct}%) at ${Date.now() - runStart}ms`);
  }
  await sleep(50);
}

const elapsedToLastReceiptMs = lastReceiptAt !== null && firstSendAt !== null ? lastReceiptAt - firstSendAt : null;
const sendWindowMs = lastSendAt !== null && firstSendAt !== null ? lastSendAt - firstSendAt : null;

for (const c of clients) {
  if (c.closedUnexpectedly) unexpectedCloses++;
  if (c.stompError) stompErrors++;
}
for (const c of clients) c.close();

const uniqueDeliveries = receivedSet.reduce((s, set) => s + set.size, 0);
const duplicateDeliveries = framesReceived.reduce((s, f) => s + f, 0) - uniqueDeliveries;
const missingDeliveries = expectedDeliveries - uniqueDeliveries;

const sorted = latencySamples.slice().sort((a, b) => a - b);
const median = (() => {
  if (!sorted.length) return null;
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
})();
const p95 = sorted.length ? sorted[Math.max(0, Math.ceil(0.95 * sorted.length) - 1)] : null;

const result = {
  generatedAt: new Date().toISOString(),
  target: 'http://localhost/ws',
  roomCode,
  clientsRequested: CLIENTS,
  clientsConnected: connected.length,
  editsPerClient: EDITS,
  editsSent: sentCount,
  expectedClientDeliveries: expectedDeliveries,
  framesReceived: framesReceived.reduce((s, f) => s + f, 0),
  uniqueDeliveries,
  missingDeliveries,
  duplicateDeliveries,
  connectionErrors,
  unexpectedCloses,
  connectionSetupMs: Number(connectionSetupMs.toFixed(2)),
  sendWindowMs: sendWindowMs !== null ? Number(sendWindowMs.toFixed(2)) : null,
  elapsedToLastReceiptMs: elapsedToLastReceiptMs !== null ? Number(elapsedToLastReceiptMs.toFixed(2)) : null,
  editsOfferedPerSecond: sendWindowMs > 0 ? Number(((sentCount / sendWindowMs) * 1000).toFixed(2)) : null,
  deliveryMessagesPerSecond:
    elapsedToLastReceiptMs > 0 ? Number(((framesReceived.reduce((s, f) => s + f, 0) / elapsedToLastReceiptMs) * 1000).toFixed(2)) : null,
  medianDeliveryLatencyMs: median !== null ? Number(median.toFixed(2)) : null,
  p95DeliveryLatencyMs: p95 !== null ? Number(p95.toFixed(2)) : null,
  latencySamples: latencySamples.length,
};
if (stompErrors) result.stompErrors = stompErrors;
result.deliveryTimelinePer500ms = timeline;

if (outPath) {
  writeFileSync(outPath, JSON.stringify(result, null, 2));
  console.log(`Wrote ${outPath}`);
}
console.log(JSON.stringify(result, null, 2));
if (missingDeliveries !== 0 || unexpectedCloses !== 0 || connectionErrors !== 0) process.exitCode = 1;
