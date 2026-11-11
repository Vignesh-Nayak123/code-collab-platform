# CodeCollab — Real-Time Collaborative Code Editor

A production-grade real-time collaborative code editor built with Spring Boot, React, Monaco Editor, WebSockets (STOMP/SockJS), Redis Pub/Sub, and Apache Kafka. Multiple users can edit code simultaneously with live cursor tracking, in-room chat, and sandboxed code execution.

[![CI](https://github.com/mhtpsd/collaborative-code-editor/actions/workflows/ci.yml/badge.svg)](https://github.com/mhtpsd/collaborative-code-editor/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=flat-square&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes&logoColor=white)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## Architecture

### Real-Time Collaboration Flow

```mermaid
flowchart LR
    subgraph Frontend
        A([User A<br/>Monaco Editor]) -->|CodeChangeMessage via STOMP WebSocket| WS
        UB([User B]) -.-|receives update| WS
        UC([User C]) -.-|receives update| WS
        WS([SockJS/STOMP<br/>Client])
    end

    subgraph Backend Cluster
        WS -->|STOMP /app/editor/roomCode/code-change| SB1[Spring Boot<br/>Instance 1]
        SB1 -->|broadcast /topic/room/code/code-change| WS
        SB1 <-->|Pub/Sub horizontal scaling| SB2[Spring Boot<br/>Instance 2]
    end

    subgraph Data Layer
        SB1 -->|update cache| RC[(Redis<br/>Cache + Pub/Sub)]
        RC <-->|sync| SB2
        SB1 -->|flush every 30s scheduled job| PG[(PostgreSQL)]
    end
```

**Real-Time Sync Flow:**
1. User types → frontend sends `CodeChangeMessage` over STOMP
2. `EditorWebSocketHandler` updates Redis cache + broadcasts to `/topic/room/{code}/code-change`
3. All connected users receive the update instantly
4. Redis Pub/Sub forwards changes to other backend instances (horizontal scaling)
5. Scheduled job flushes Redis content → PostgreSQL every 30 seconds

---

### Code Execution Flow

```mermaid
flowchart TD
    subgraph API Layer
        U([User clicks Run]) -->|POST /api/v1/execute| API[Spring Boot<br/>REST Controller]
        API -->|save PENDING status| DB[(PostgreSQL)]
        FE([Frontend]) -->|poll GET /api/v1/execute/id| API
        API -->|return result| FE
    end

    subgraph Kafka Pipeline
        API -->|publish| KT[Kafka Topic<br/>execution-requests]
        KT -->|consume| KC[ExecutionResult<br/>Consumer]
        KC -->|save result| DB
    end

    subgraph Docker Sandbox
        KC -->|spin up container --network none --memory=256m| DC[Docker Container]
        DC -->|capture stdout/stderr| KC
    end
```

**Code Execution Flow:**
1. User clicks Run → `POST /api/v1/execute`
2. Backend saves `PENDING` result → publishes to Kafka `execution-requests`
3. `ExecutionResultConsumer` picks up message → spins up sandboxed Docker container
4. Output/error captured → saved to PostgreSQL
5. Frontend polls `GET /api/v1/execute/{id}` until completion

---

## Features

- 👥 **Real-time collaboration** — multiple users edit simultaneously with live cursor tracking
- 💬 **In-room chat** — integrated chat alongside the code editor
- 🎨 **Monaco Editor** — VS Code's editor engine with syntax highlighting for 6 languages
- 🔒 **Sandboxed execution** — code runs in Docker containers with `--network none`, 256MB memory limit, 10s timeout
- 📡 **WebSocket (STOMP/SockJS)** — bidirectional real-time communication with auto-reconnect
- 🔄 **Redis Pub/Sub** — enables horizontal scaling across multiple backend instances
- ⚡ **Kafka execution queue** — decouples code submission from execution for reliability
- ☸️ **Kubernetes-ready** — sticky sessions for WebSocket affinity, HPA autoscaling, Kustomize overlays
- 🧪 **Testcontainers** — integration tests with real PostgreSQL, Redis, Kafka

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Java 17 |
| WebSocket | STOMP over SockJS |
| Frontend | React 18, TypeScript, Vite |
| Editor | Monaco Editor (@monaco-editor/react) |
| Database | PostgreSQL 16 + Flyway migrations |
| Cache / Pub-Sub | Redis 7 |
| Messaging | Apache Kafka |
| Code Execution | Docker (sandboxed, network-disabled) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containers | Docker + Docker Compose |
| Kubernetes | K8s manifests + Kustomize |
| CI/CD | GitHub Actions |

---

## Project Structure

```
collaborative-code-editor/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/mohitprasad/codeeditor/
│   │   ├── config/                   # WebSocket, Redis, Kafka, CORS, OpenAPI
│   │   ├── controller/               # REST: Room, Execution, Health
│   │   ├── websocket/                # STOMP handler + event listener
│   │   │   └── dto/                  # WS message DTOs
│   │   ├── service/                  # Business logic + Redis pub/sub
│   │   ├── kafka/                    # Producer + Consumer
│   │   ├── execution/                # Docker sandbox execution
│   │   ├── model/                    # Entities, DTOs, Enums
│   │   ├── repository/               # Spring Data JPA repositories
│   │   └── exception/                # Global exception handler
│   ├── src/main/resources/
│   │   ├── application.yml           # Main config
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/V1__create_tables.sql
│   └── src/test/                     # Unit + integration tests
├── frontend/                         # React application
│   └── src/
│       ├── components/               # Editor, Room, Chat, Presence, common
│       ├── hooks/                    # useWebSocket, useRoom, useCodeExecution
│       ├── services/                 # api.ts, websocket.ts
│       ├── types/                    # TypeScript interfaces
│       └── utils/                    # colors.ts
├── k8s/                              # Kubernetes manifests
│   ├── base/                         # Postgres, Redis, Kafka, Backend, Frontend
│   └── overlays/dev|prod/            # Kustomize environment overrides
├── .github/workflows/                # CI + Release pipelines
├── docker-compose.yml                # Local dev stack
└── docker-compose.prod.yml           # Production compose
```

---

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local backend dev)
- Node.js 20+ (for local frontend dev)

### Run with Docker Compose

```bash
# Clone
git clone https://github.com/mhtpsd/collaborative-code-editor.git
cd collaborative-code-editor

# Start the full stack
docker compose up --build

# Open your browser
open http://localhost
```

Services:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator/health

### Local Development

**Backend:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

---

## API Reference

### Rooms

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/rooms` | Create room → returns `roomCode` |
| `GET` | `/api/v1/rooms/{roomCode}` | Room info + active user count |
| `DELETE` | `/api/v1/rooms/{roomCode}` | Close room |
| `GET` | `/api/v1/rooms/{roomCode}/document` | Get current document |

### Code Execution

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/execute` | Submit code (async, Kafka-backed) |
| `GET` | `/api/v1/execute/{id}` | Poll result |
| `GET` | `/api/v1/execute/{id}/status` | Get status only |

### WebSocket (STOMP)

Connect to `/ws` with SockJS. Subscribe to topics after connecting:

| Destination | Direction | Description |
|------------|-----------|-------------|
| `/app/editor/{roomCode}/code-change` | SEND | Broadcast code edit |
| `/app/editor/{roomCode}/cursor` | SEND | Broadcast cursor position |
| `/app/editor/{roomCode}/chat` | SEND | Send chat message |
| `/topic/room/{roomCode}/code-change` | SUBSCRIBE | Receive code changes |
| `/topic/room/{roomCode}/cursor` | SUBSCRIBE | Receive cursor updates |
| `/topic/room/{roomCode}/chat` | SUBSCRIBE | Receive chat messages |
| `/topic/room/{roomCode}/user-joined` | SUBSCRIBE | User join events |
| `/topic/room/{roomCode}/user-left` | SUBSCRIBE | User leave events |

---

## Supported Languages

| Language | Docker Image | Timeout |
|----------|-------------|---------|
| JavaScript | `node:18-alpine` | 10s |
| Python | `python:3.11-alpine` | 10s |
| Java | `openjdk:17-alpine` | 10s |
| C++ | `gcc:13-alpine` | 10s |
| Go | `golang:1.21-alpine` | 10s |
| Rust | `rust:1.75-alpine` | 10s |

Execution sandbox: `--network none`, `--memory=256m`, `--cpus=0.5`, `--read-only`

---

## Kubernetes Deployment

```bash
# Dev
kubectl apply -k k8s/overlays/dev/

# Prod
kubectl apply -k k8s/overlays/prod/
```

---

## Running Tests

```bash
# Backend unit + integration tests
cd backend && mvn test

# Frontend type check + build
cd frontend && npm run type-check && npm run build
```

---


