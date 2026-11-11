# WebSocket broadcast performance: before/after (100 clients × 20 edits, 2 replicas)

Workload: 100 STOMP clients connect to `http://localhost/ws` (nginx → backend1/backend2
round-robin), all subscribe to `/topic/room/6WCOUQ7D/code-change`, every client sends
20 code changes → 2,000 edits offered, 200,000 expected client deliveries.
Harness: `loadtest/ws-100x20.mjs` (reconstruction of the lost resume-step5 harness;
identical workload, metrics schema and formulas verified against
`resume-step5-websocket-100x20-20261111.json`).

## Latency measurement (verified)

Latency = `recvAt − sentAt` where the sender stamps
`performance.timeOrigin + performance.now()` into the payload immediately before the
STOMP SEND frame and the receiver records `recvAt` at WebSocket `onmessage` entry
(same host/process clock, nearest-rank p95). Verified correct:

- Client-side processing is negligible: the Node harness used <9 s CPU total per run
  (~few % of one core) while the two backends burned 2.5–5 cores. The large p95 was
  genuine server-side queueing, not a harness artifact.
- Formula cross-check vs resume-step5: `editsOfferedPerSecond = edits/sendWindow` and
  `deliveryMessagesPerSecond = frames/elapsedToLastReceipt` reproduce its numbers
  exactly for the same workload.

## Bottleneck (identified before changing code)

Thread dumps (SIGQUIT) during baseline runs showed the `clientOutboundChannel`
executor threads pinned in `java.util.zip.Deflater.deflate →
org.apache.tomcat.websocket.PerMessageDeflate.sendMessagePart`: Tomcat 10.1.x
negotiates the **permessage-deflate WebSocket extension by default**, so every one of
the 200,000 small STOMP frames was independently deflate-compressed. Handshake probe
confirmed `Sec-WebSocket-Extensions: permessage-deflate;client_max_window_bits=15`
accepted whenever a client offers it (browsers and Node's WebSocket do by default).

## The one optimization

`backend/src/main/java/com/cce/codeeditor/config/WebSocketNoCompressionFilter.java`
(NEW): strips `Sec-WebSocket-Extensions` from WebSocket upgrade requests so Tomcat
never engages permessage-deflate. RFC 6455-legal (extension is optional/negotiated);
payloads, ordering, delivery counts, Redis fanout, Kafka and persistence untouched;
frames are simply uncompressed. Backend images rebuilt; no other change.

## Results (warm-up + 3 runs each; extra steady-state runs noted)

| Metric | Before (baseline) | After (deflate off) |
|---|---|---|
| p95 delivery latency | 18.51s / 12.10s / 21.90s | 9.77s* / 3.74s / 3.61s (run4 3.50s, run5 4.94s) |
| Median delivery latency | 12.10s / 8.04s / 16.84s | 7.74s* / 2.49s / 1.81s (run4 1.82s, run5 1.84s) |
| Deliveries/sec | 10,304 / 15,748 / 8,746 | 19,400* / 46,810 / 48,781 (run4 50,389, run5 35,103) |
| Edits offered/sec | 9,497 / 9,641 / 7,494 | 3,257* / 5,039 / 4,931 |
| Frames received | 200,000 / 200,000 / 200,000 | 200,000 / 200,000 / 200,000 |
| Missing deliveries | 0 / 0 / 0 | 0 / 0 / 0 |
| Duplicate deliveries | 0 / 0 / 0 | 0 / 0 / 0 |
| Connection errors | 0 / 0 / 0 | 0 / 0 / 0 |
| Unexpected closes | 0 / 0 / 0 | 0 / 0 / 0 |

`*` = first run after container restart (cold JIT/Hibernate); steady state is runs 2–5.

**Improvement (steady state): p95 12.1–21.9s → 3.5–4.9s (≈4–6×), deliveries/sec
8.7k–15.7k → 35k–50k (≈3–6×).** Reliability unchanged: every run before and after
delivered exactly 200,000/200,000 frames, 0 missing, 0 duplicates.

## Honest caveats

- The original resume-step5 harness source could not be recovered (searched session
  DB, snapshot store, VS Code/shell history, temp dirs); the harness was reconstructed
  to the same schema and workload. The baseline reproduced the original pathology
  (all 200k delivered, p95 12–25s), so the before/after comparison is valid.
- Results vary run-to-run (system under mixed load); steady-state values are
  consistent but run1-after was an outlier.
- Remaining, intentionally unchanged bottlenecks (each would be a separate
  optimization): per-Redis-message thread churn (`RedisMessageListenerContainer`
  defaults to a new thread per message — thread counter grows ~200/2s), synchronous
  per-edit DB update with `show-sql: true` in the dev profile, and the
  publish-only handler (no local fast-path broadcast).

Instrumentation artifacts: `loadtest/ws-baseline-*.json`, `loadtest/ws-after-*.json`,
thread-dump/CPU series in `ws-baseline-cpu2.json` and `ws-after-instr.json`.
