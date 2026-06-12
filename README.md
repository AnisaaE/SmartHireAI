# SmartHireAI

SmartHireAI is a microservices-based recruitment platform that uses local LLMs through Ollama to support CV screening and candidate-to-job matching. The system is split into independent services responsible for authentication, document processing, job management, applications, and AI analysis.

## Overview

The platform supports:

- candidate and recruiter registration and login
- job posting creation and management
- CV and job description upload and processing
- applying to specific job positions
- automated AI-based candidate analysis and ranking

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
| Frontend | User interface | Frontend app | 3000 / 5173 |

The detailed technical specification is available in [architecture.md](./architecture.md).

## Main Workflow

1. A recruiter registers or logs into the system.
2. The recruiter creates a job posting.
3. If needed, the recruiter uploads a job description document.
4. A candidate uploads a CV and applies to a specific job.
5. The system collects the relevant job information and documents.
6. The AI analysis service evaluates the submitted applications.
7. The result returns a ranked list of candidates with short reasoning.

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

## Screenshots

You can add application screenshots here later.

Example structure:

```md
## Screenshots

### Login
![Login screen](./docs/screenshots/login.png)

### Recruiter Dashboard
![Recruiter dashboard](./docs/screenshots/recruiter-dashboard.png)

### Candidate Flow
![Candidate flow](./docs/screenshots/candidate-flow.png)
```

If you want, you can create a folder such as:

```text
docs/screenshots/
```

and keep all images there.

## Useful Files

- [architecture.md](./architecture.md) - technical architecture and endpoint specification
- [LOCAL_DEV.md](./LOCAL_DEV.md) - recommended local development setup
- [RUNNING_LOCALLY.md](./RUNNING_LOCALLY.md) - quick local run instructions and smoke test

## Notes

The project is designed around a modular architecture and future extensibility. It can later be expanded with additional integrations, persistent storage for analysis history, and more complete gateway routes for the frontend.
