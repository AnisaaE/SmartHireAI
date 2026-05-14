# SmartHireAI Local Dev

## Recommended Approach

The easiest path is:

1. Run only infrastructure in Docker.
2. Run every application locally from your IDE or terminal.
3. Verify the backend services first.
4. Start the frontend last.
5. Containerize the services only after the local flow is stable.

This repo is closer to that setup already, and it is much easier to debug than full container orchestration from the start.

## Infrastructure Only

Start the required databases and caches:

```powershell
docker compose -f docker-compose.local.yml up -d
```

This starts:

- PostgreSQL on `localhost:5432`
- MongoDB on `localhost:27017`
- Redis on `localhost:6379`

PostgreSQL creates these databases automatically:

- `smarthire_auth`
- `smarthire_jobs`
- `smarthire_application`

## Required Local Runtime

- Java 21
- Maven
- Node.js
- Docker Desktop
- Ollama running locally on `http://localhost:11434`

Optional Ollama check:

```powershell
ollama list
ollama pull llama3.2:latest
```

## Start Order

Open a separate terminal for each service.

### 1. Auth Service

```powershell
cd auth
mvn spring-boot:run
```

Runs on `http://localhost:8081`.

### 2. Document Service

```powershell
cd document
mvn spring-boot:run
```

Runs on `http://localhost:8082`.

### 3. AI Analysis Service

```powershell
cd ai_analysis
mvn spring-boot:run
```

Runs on `http://localhost:8083`.

### 4. Job Service

```powershell
cd job
mvn spring-boot:run
```

Runs on `http://localhost:8084`.

### 5. Application Service

```powershell
cd application
mvn spring-boot:run
```

Runs on `http://localhost:8085`.

### 6. Dispatcher

```powershell
cd dispatcher
mvn spring-boot:run
```

Runs on `http://localhost:8080`.

### 7. Frontend

```powershell
cd frontend
npm install
npm run dev
```

Vite usually starts on `http://localhost:5173`.

If your local Node version is too old for Vite, you can run the frontend in Docker instead:

```powershell
docker build -t smarthire-frontend-dev .\frontend
docker run --rm -p 5173:5173 -e VITE_API_PROXY_TARGET=http://host.docker.internal:8080 smarthire-frontend-dev
```

On Windows Docker Desktop, `host.docker.internal` lets the container reach the locally running `dispatcher`.

## Important Note

The local infrastructure is now aligned, but the `dispatcher` currently exposes only a small subset of the routes used by the frontend. That means:

- backend services can run locally
- some direct API testing can work
- the full frontend flow through `http://localhost:8080` is not complete yet

## What To Test First

Test in this order:

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. `GET /api/jobs`
4. document upload and fetch
5. application creation
6. AI analysis

If you want the whole UI to work end-to-end, the next step is finishing the missing gateway routes in `dispatcher`.
