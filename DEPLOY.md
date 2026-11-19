# Free deployment: Vercel (frontend) + Render (backend)

This is a **demo** deployment: real-time editing, cursors and chat work, but
**code execution is turned off** and Kafka isn't deployed. The full stack
(Kafka + Docker sandbox) still runs locally with `docker compose up`.

```
Browser ──HTTPS──▶ Vercel (static React build)
   │
   ├──HTTPS REST (/api/v1)──────────┐
   └──SockJS/WebSocket (/ws)────────┤
                                    ▼
                     Render web service (Docker, free, 512 MB)
                     SPRING_PROFILES_ACTIVE=prod,demo
                       ├── Render Postgres (free)   rooms, documents, run records
                       └── Render Key Value (free)  doc cache + code-change pub/sub
```

## What the `demo` profile changes

| Setting | Full stack | Demo (`prod,demo`) |
|---|---|---|
| `app.execution.mode` (`EXECUTION_MODE`) | `kafka` | `disabled`: Run returns "Code execution is disabled…" |
| Kafka beans / auto-config | on | off (no broker needed) |
| Hikari pool / Tomcat threads | 10 / 200 | 4 / 40 |
| Actuator | health, info, metrics, full details | health only, no details |
| CORS + WebSocket origins | `*` | whatever `ALLOWED_ORIGINS` says |

There's also an `inline` mode (no Kafka, runs one at a time in-process) for a single
VPS that **does** have Docker. Don't use it on Render: there is no Docker daemon there.

## What breaks or degrades on Render's free tier

| Constraint | Effect on this app | Mitigation in this repo |
|---|---|---|
| **No Docker daemon / no privileged containers** | Code execution can't work: the backend runs `docker run` per execution. | `EXECUTION_MODE=disabled`. **Never** run user code directly in the web container (see Security). |
| **512 MB RAM, 0.1 CPU** | Default JVM (75% heap + metaspace + threads) can be OOM-killed. Boot takes ~2–3 min at 0.1 CPU (measured locally with `--cpus=0.1 --memory=512m`; it settled at ~225 MB). | `JAVA_OPTS` in `render.yaml`: `-Xmx224m`, capped metaspace/code cache, SerialGC, C1-only JIT, exit on OOM. Smaller pools/threads in `application-demo.yml`. |
| **Spins down after ~15 min without inbound traffic** | First request after idle waits for a full cold boot (~2–3 min for this app). All WebSockets drop; the in-memory STOMP broker state is lost. | Frontend pings `/api/health` on page load (`warmUpBackend`). STOMP client reconnects every 5 s and resubscribes. Document text survives in Postgres (flushed every 30 s) and Key Value. |
| **Ephemeral disk** | Lost on every deploy/restart. Only execution temp files are written there (unused in demo). | Nothing durable on local disk: state lives in Postgres/Key Value. |
| **Free Key Value: 25 MB, not persisted** | A Key Value restart loses the doc cache and any edits from the last ≤30 s that weren't flushed to Postgres. | `allkeys-lru`; documents reload from Postgres on miss. |
| **Free Postgres expires 30 days after creation** | Database (and all rooms) is deleted unless upgraded. | Recreate it, or point `DB_URL` at a free Neon/Supabase Postgres (add `?sslmode=require`). |
| **Kafka** | Not offered on Render's free tier. | Demo mode removes the dependency entirely. |
| **WebSocket origin / CORS** | Vercel and Render are different origins; the old config allowed `*`. | `ALLOWED_ORIGINS` drives both the REST CORS filter and the `/ws` SockJS origin check. |
| **Single instance** | Redis pub/sub fan-out isn't needed but is still the path code changes take, so Key Value is required. | Keep Key Value (free). |
| **Vercel can't proxy WebSockets** | `/ws` can't go through a Vercel rewrite. | Frontend calls Render directly via `VITE_API_URL` / `VITE_WS_URL`. |

## Step-by-step

### 0. Prerequisites
- Push this repo to GitHub (Render and Vercel both deploy from Git).
- Decide your Vercel project name now, e.g. `my-editor`, so your origin will be
  `https://my-editor.vercel.app`. (If the name's taken, Vercel adds a suffix; you'll
  fix `ALLOWED_ORIGINS` in step 3.)

### 1. Backend on Render (Blueprint)
1. Render Dashboard → **New → Blueprint** → connect the repo. Render detects `render.yaml`.
2. It lists 3 resources: `cce-backend` (web, free), `cce-postgres` (free), `cce-redis` (Key Value, free).
3. When prompted for **`ALLOWED_ORIGINS`**, enter `https://my-editor.vercel.app` (no trailing slash).
4. **Apply**. The first Docker build takes ~5–10 min (Maven downloads); the first boot takes another 1–3 min.
5. When it's live, open `https://cce-backend-XXXX.onrender.com/api/health` (use the URL Render shows). Expect
   `{"status":"UP", "database":"UP", "redis":"UP"}`.

> If the Blueprint rejects a field (Render renames things now and then, e.g. Key Value
> was formerly `type: redis`), create the three resources by hand in the dashboard with the
> same names and env vars. `render.yaml` doubles as the checklist.

### 2. Frontend on Vercel
1. Vercel → **Add New → Project** → import the repo.
2. **Root Directory: `frontend`**. Framework preset: Vite (`frontend/vercel.json` also sets build/output).
3. Environment Variables (Production, and Preview if you want previews to work):
   - `VITE_API_URL` = `https://cce-backend-XXXX.onrender.com/api/v1`
   - `VITE_WS_URL`  = `https://cce-backend-XXXX.onrender.com/ws`
4. **Deploy**. These are build-time values: after changing them, **redeploy**.

### 3. Lock the origins
1. Copy the production domain Vercel actually assigned.
2. Render → `cce-backend` → Environment → set `ALLOWED_ORIGINS` to it (comma-separate several;
   patterns are allowed, e.g. `https://my-editor.vercel.app,https://my-editor-*-myteam.vercel.app`
   for preview deploys). Save; Render redeploys.

### 4. Smoke test
1. Open the Vercel URL. If the backend was asleep, give it a minute.
2. Create a room, open the room link in a second browser/incognito window, type in one window and watch it appear in the other.
3. Click **Run**. The output panel should show "Code execution is disabled on this demo deployment…".
4. In DevTools → Network, the `/ws/...` request should be `101 Switching Protocols` (WebSocket transport).
   A 403 on `/ws/info` or a CORS error means `ALLOWED_ORIGINS` doesn't match the page's origin exactly.

### Troubleshooting
| Symptom | Cause / fix |
|---|---|
| Render deploy fails health check | Boot is slow at 0.1 CPU; check logs for `Started CodeEditorApplication`. Look for DB/Redis connection errors (resources must share a region). |
| Instance restarts with exit 3 / OOM | Lower `-Xmx` (e.g. 192m) or `-XX:MaxMetaspaceSize`. |
| `Invalid CORS request` / 403 on `/ws/info` | `ALLOWED_ORIGINS` mismatch (scheme, subdomain, trailing slash). |
| Frontend calls `/api/v1/...` on the Vercel domain (404) | `VITE_API_URL` wasn't set at build time; set it and redeploy. |
| "The URL's scheme must be either 'http:' or 'https:'" | Fixed: `ws://`/`wss://` in `VITE_WS_URL` is now converted for SockJS. |

## Security: read before enabling code execution anywhere

The demo turns execution off. If you ever turn it on (`kafka`/`inline`) on a public host:

- **Never run user code without an isolation boundary.** "Just run `python` in the backend
  container" hands every visitor a shell holding your `DB_PASSWORD` and Redis URL, with
  outbound network access.
- **The Docker-socket design is root-equivalent on the host.** `docker-compose.yml` mounts
  `/var/run/docker.sock` into the backend. Anyone who gets code execution in the backend
  container gets root on the host.
- **Timeouts don't stop the container.** `DockerExecutionService` calls `destroyForcibly()` on
  the `docker` CLI process. That kills the client, but the container (e.g. `while True: pass`)
  keeps running. Name the container and `docker kill` it, or wrap the command in `timeout`.
- **Sandbox flags missing:** add `--pids-limit` (fork bombs), `--cap-drop=ALL`,
  `--security-opt=no-new-privileges`, and `--user 65534:65534`.
- **No auth or rate limiting** on `POST /api/v1/execute` (or `DELETE /api/v1/rooms/{code}`).
  Anyone can queue unlimited runs or close any room.
- **Output read only after exit:** a program printing >64 KB blocks on the pipe and is reported as a timeout.
- Prefer a purpose-built sandbox service (e.g. a self-hosted Judge0/Piston on a VM you control,
  or gVisor/Firecracker) over raw `docker run`.

## Secrets
No secrets are committed: `render.yaml` pulls DB credentials from the managed database and
prompts for `ALLOWED_ORIGINS`. `.env*` files are gitignored except the `.env.example` templates.
`VITE_*` values are compiled into public JavaScript, so never put a secret in one.
