# SmartHireAI

SmartHireAI is a microservices-based recruitment platform that uses local LLMs through Ollama to support CV screening and candidate-to-job matching. The system is split into independent services responsible for authentication, document processing, job management, applications, and AI analysis.

## Overview

The platform supports:

- candidate and recruiter registration and login
- job posting creation and management
- CV and job description upload and processing
- applying to specific job positions
- automated AI-based candidate analysis and ranking

## Project Objective

SmartHireAI is designed to reduce manual pre-screening effort and help recruiters identify suitable candidates more efficiently.

The project aims to:

- centralize candidate, job, and document data
- process CVs and job descriptions into analyzable content
- evaluate candidate-job compatibility with AI-supported scoring
- provide a modular and maintainable Java-based architecture

## Architecture

The project is built as a set of microservices:

| Service | Role | Technologies | Port |
| --- | --- | --- | --- |
| Dispatcher | Gateway and client entry point | Spring Cloud Gateway | 8080 |
| Auth Service | Authentication, JWT, roles | Spring Security, PostgreSQL | 8081 |
| Document Service | Upload and text extraction for documents | Spring AI, Apache Tika, MongoDB | 8082 |
| AI Analysis Service | Candidate analysis, scoring, and ranking | Spring AI, Ollama, Redis | 8083 |
| Job Service | Job posting management | Spring Boot, PostgreSQL | 8084 |
| Application Service | Job applications and application status | Spring Boot, PostgreSQL | 8085 |
| Frontend | User interface | React, Vite | 3000 / 5173 |

The detailed technical specification is available in [architecture.md](./architecture.md).

## User Roles

- Recruiter - creates job postings, reviews applications, and checks AI analysis results
- Candidate - uploads a CV, applies to jobs, and tracks application activity

## Main Workflow

1. A recruiter registers or logs into the system.
2. The recruiter creates a job posting.
3. If needed, the recruiter uploads a job description document.
4. A candidate uploads a CV and applies to a specific job.
5. The system collects the relevant job information and documents.
6. The AI analysis service evaluates the submitted applications.
7. The result returns a ranked list of candidates with short reasoning.

## Data Management

The system uses multiple persistence layers based on data type:

- PostgreSQL - stores users, job postings, and applications
- MongoDB - stores document metadata and extracted content
- Redis - stores AI analysis outputs for fast retrieval

## Project Structure

- `auth` - user management, roles, and JWT
- `document` - uploads, metadata, and text extraction
- `job` - job creation and management
- `application` - applications and application statuses
- `ai_analysis` - AI analysis and scoring
- `dispatcher` - gateway for backend services
- `frontend` - web client
- `desktop` - desktop client
- `mobile` - mobile client

## Running Locally

There are two convenient ways to run the project locally.

### Option 1: Infrastructure only in Docker

Start the supporting services:

```powershell
docker compose -f docker-compose.local.yml up -d
```

Then run the application services locally from your IDE or terminal. Full steps are available in [LOCAL_DEV.md](./LOCAL_DEV.md).

### Option 2: Full Docker Compose setup

```powershell
docker compose up --build
```

This starts:

- PostgreSQL
- MongoDB
- Redis
- all backend services
- the frontend

## Requirements

For local development, you will need:

- Java 21
- Maven
- Node.js
- Docker Desktop
- Ollama

## Technology Stack

- Backend: Java 21, Spring Boot, Spring Security, Spring Cloud Gateway
- Data: PostgreSQL, MongoDB, Redis
- AI and document processing: Ollama, Spring AI, Apache Tika
- Frontend: React, Vite
- Tooling and testing: Maven, Docker, Docker Compose, k6

Example Ollama model setup:

```powershell
ollama pull llama3.2
```

## Main API Groups

Requests are routed through the `dispatcher` to these API groups:

- `/api/auth/**`
- `/api/documents/**`
- `/api/jobs/**`
- `/api/applications/**`
- `/api/analysis/**`

One of the important document endpoints is:

```text
GET /api/documents/content/{id}
```

It returns the extracted raw text of a document, which is then used by the AI analysis service.

## Performance Test Results

The project includes k6-based performance testing for the API layer.

### Baseline k6 Run

- Run date: `2026-05-15`
- Total requests: `3497`
- Request rate: `58.16 req/s`
- Check success rate: `100%`
- HTTP failure rate: `0%`
- Overall average latency: `36.95 ms`
- Overall p95 latency: `111.95 ms`

Scenario durations and p95 latencies:

- Auth Login - `50s` active window, `129.37 ms` p95
- Recruiter Reads - `30s`, `61.82 ms` p95
- Application Reads - `30s`, `63.80 ms` p95
- Analysis Report - `30s`, `116.36 ms` p95
- Analysis Start - `30s`, `179.05 ms` p95

### Realistic Mixed-Load Run

- Run date: `2026-05-15`
- Max virtual users: `74`
- Check success rate: `100%`
- Overall average latency: `14.56 ms`
- Overall p95 latency: `30.92 ms`
- k6 `http_req_failed`: `4.82%`
- Unexpected system errors: `0%`

Scenario durations and p95 latencies:

- Recruiter Reads - `45s`, `14.57 ms` p95
- Candidate Reads - `45s`, `16.19 ms` p95
- Apply Contention - `45s`, `75.94 ms` p95
- Analysis Report Reads - `45s`, `77.34 ms` p95
- Analysis Starts - `45s`, `48.21 ms` p95

The `4.82%` failure figure in the mixed-load run comes from expected `409 Conflict` business-rule collisions during repeated apply attempts, not from backend instability.

## Screenshots

### Screen 1
![Application screenshot 1](./document/img/Screenshot%202026-06-12%20160623.png)

### Screen 2
![Application screenshot 2](./document/img/Screenshot%202026-06-12%20162027.png)

### Screen 3
![Application screenshot 3](./document/img/Screenshot%202026-06-12%20162311.png)

### Screen 4
![Application screenshot 4](./document/img/Screenshot%202026-06-12%20162350.png)

### Screen 5
![Application screenshot 5](./document/img/Screenshot%202026-06-12%20162451.png)

## Useful Files

- [architecture.md](./architecture.md) - technical architecture and endpoint specification
- [LOCAL_DEV.md](./LOCAL_DEV.md) - recommended local development setup
- [RUNNING_LOCALLY.md](./RUNNING_LOCALLY.md) - quick local run instructions and smoke test
- [performance/README.md](./performance/README.md) - how to run the k6 performance tests
- [performance/K6_RUN_REPORT.md](./performance/K6_RUN_REPORT.md) - baseline performance test report
- [performance/K6_REALISTIC_RUN_REPORT.md](./performance/K6_REALISTIC_RUN_REPORT.md) - realistic mixed-load performance report

## Notes

The project is designed around a modular architecture and future extensibility. It can later be expanded with additional integrations, persistent storage for analysis history, and more complete gateway routes for the frontend.
