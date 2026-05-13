# SmartHireAI - Technical Specification & Architecture

## 1. System Overview

SmartHireAI is a microservices-based recruitment platform that uses local LLMs through Ollama to automate candidate screening. The platform accepts job descriptions and candidate CVs, extracts their text, runs AI-based analysis, and returns ranked candidates for a given role.

### Tech Stack Summary

| Service | Technology | Database | Port |
| --- | --- | --- | --- |
| Dispatcher | Spring Cloud Gateway | - | 8080 |
| Auth Service | Spring Security 6, JWT | PostgreSQL | 8081 |
| Document Service | Spring AI, Apache Tika | MongoDB | 8082 |
| AI Analysis Service | Spring AI, Ollama (Llama3.2/Mistral) | Redis | 8083 |

---

## 2. Service Specifications

### 2.1. Auth Service

**Role:** Identity, authentication, authorization, and user profile ownership.

**Key Features**

- User registration and login
- JWT generation and validation
- Role-based access control with `RECRUITER` and `CANDIDATE`
- Ownership checks for documents and analysis requests

**Core Endpoints**

- `POST /api/auth/register` - Create a new account
- `POST /api/auth/login` - Exchange credentials for a JWT
- `GET /api/auth/validate` - Internal token validation for gateway/service-to-service use
- `GET /api/auth/users/{id}` - Get public user profile/identity metadata
- `PUT /api/auth/users/{id}` - Update user profile fields such as username or email
- `PUT /api/auth/users/{id}/password` - Change password securely after credential verification
- `PUT /api/auth/users/{id}/role` - Admin-only role update when account permissions need to change
- `DELETE /api/auth/users/{id}` - Deactivate or remove user account

**Notes**

- `DELETE` should usually be implemented as soft delete or deactivation to preserve auditability
- `PUT /role` must be restricted to admin/internal use only

### 2.2. Document Service

**Role:** Handles uploaded files, text extraction, metadata storage, and document lifecycle.

**Key Features**

- Multipart PDF upload
- Text extraction from CVs and job descriptions
- Metadata storage for search and filtering
- Ownership-based document access

**Core Endpoints**

- `POST /api/documents/upload` - Upload a PDF document
- `GET /api/documents/{id}` - Get document metadata
- `GET /api/documents/owner/{userId}` - List all documents owned by a user
- `GET /api/documents/cv/{candidateId}` - Get the active CV document for a candidate
- `GET /api/documents/content/{id}` - Retrieve extracted raw text for AI consumption
- `PUT /api/documents/{id}` - Update document metadata such as title, tags, or document type
- `PUT /api/documents/{id}/content` - Replace extracted text after re-processing or manual correction
- `PUT /api/documents/{id}/reprocess` - Re-run extraction pipeline for a stored file
- `DELETE /api/documents/{id}` - Remove a document and its extracted content

**Notes**

- `PUT /content` is useful when OCR or parser output needs correction
- `PUT /reprocess` is important when extraction logic or prompt settings improve over time

### 2.3. Job Service

**Role:** Manages job postings created by recruiters and exposes them for candidate browsing and application.

**Key Features**

- Recruiter job creation and editing
- Public or authenticated job listing
- Job lifecycle management
- Job ownership by recruiter

**Core Endpoints**

- `POST /api/jobs` - Create a new job posting
- `GET /api/jobs` - List available jobs for candidates
- `GET /api/jobs/{id}` - Get job details
- `GET /api/jobs/recruiter/{recruiterId}` - List jobs created by a recruiter
- `PUT /api/jobs/{id}` - Update job posting details
- `PUT /api/jobs/{id}/status` - Open, close, archive, or publish a job
- `DELETE /api/jobs/{id}` - Remove or archive a job posting

**Recommended Status Values**

- `DRAFT`
- `OPEN`
- `CLOSED`
- `ARCHIVED`

### 2.4. Application Service

**Role:** Connects candidates to jobs through explicit applications and keeps the recruitment process traceable.

**Key Features**

- Candidate applies to a specific job
- One candidate can apply to many jobs
- Each application references the CV used for that job
- Recruiters can track candidate progress per job

**Core Endpoints**

- `POST /api/applications` - Apply to a specific job
- `GET /api/applications/{id}` - Get application details
- `GET /api/applications/job/{jobId}` - List all applications for a job
- `GET /api/applications/candidate/{candidateId}` - List all applications made by a candidate
- `PUT /api/applications/{id}` - Update application fields such as selected CV or cover letter
- `PUT /api/applications/{id}/status` - Update recruitment status for that application
- `DELETE /api/applications/{id}` - Withdraw or remove an application

**Recommended Status Values**

- `APPLIED`
- `UNDER_REVIEW`
- `SHORTLISTED`
- `REJECTED`
- `HIRED`
- `WITHDRAWN`

### 2.5. AI Analysis Service

**Role:** Matches job descriptions with candidate CVs and returns ranked results.

**Key Features**

- Multi-step analysis pipeline
- Candidate filtering, scoring, and ranking
- Result caching in Redis
- Re-runnable analyses when job requirements or applicant pool changes

**Core Endpoints**

- `POST /api/analysis/start` - Start analysis for one job and its submitted applications
- `GET /api/analysis/{analysisId}` - Get analysis metadata and status
- `GET /api/analysis/report/{jobId}` - Fetch ranked results for a job
- `PUT /api/analysis/{analysisId}` - Update analysis configuration such as scoring weights or evaluation criteria
- `PUT /api/analysis/{analysisId}/restart` - Re-run an existing analysis with the same or updated inputs
- `PUT /api/analysis/{analysisId}/status` - Internal/manual status correction for queued, running, completed, or failed
- `DELETE /api/analysis/{analysisId}` - Remove cached analysis result and report data

**Recommended Response States**

- `QUEUED`
- `RUNNING`
- `COMPLETED`
- `FAILED`
- `CANCELLED`

**Optional but Useful Endpoints**

- `GET /api/analysis/{analysisId}/candidates` - Inspect per-candidate scores and reasoning
- `DELETE /api/analysis/{analysisId}/cache` - Clear only cache without deleting the analysis record

### 2.6. Dispatcher Service

**Role:** Single entry point for clients, routing requests to downstream services and enforcing security policies.

**Responsibilities**

- Route external traffic to internal services
- Verify JWT before forwarding secured requests
- Apply CORS, rate limiting, and request logging
- Hide internal service topology from frontend clients

**Typical Routed API Surface**

- `/api/auth/**`
- `/api/documents/**`
- `/api/jobs/**`
- `/api/applications/**`
- `/api/analysis/**`

---

## 3. Data Models

### PostgreSQL - Auth Service

**User**

- `Long id`
- `String username`
- `String email`
- `String password` (BCrypt hash)
- `UserRole role`
- `Boolean active`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

### PostgreSQL - Job Service

**Job**

- `Long id`
- `Long recruiterId`
- `String title`
- `String description`
- `String location`
- `String employmentType`
- `String status`
- `String jobDocumentId`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

### PostgreSQL - Application Service

**Application**

- `Long id`
- `Long jobId`
- `Long candidateId`
- `String cvDocumentId`
- `String status`
- `LocalDateTime appliedAt`
- `LocalDateTime updatedAt`

### MongoDB - Document Service

**Document**

- `String id`
- `String ownerId`
- `DocType type` (`JOB`, `CV`)
- `String fileName`
- `String title`
- `String rawTextContent`
- `String storagePath` or blob reference
- `String status`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`

### Redis / Persistent Store - AI Analysis Service

**AnalysisResult**

- `String analysisId`
- `String jobId`
- `List<Long> applicationIds`
- `Map<Long, Double> applicationScores`
- `Map<Long, String> applicationReasoning`
- `String status`
- `String summary`
- `LocalDateTime createdAt`
- `LocalDateTime updatedAt`
- `Duration ttl`

---

## 4. Recommended API Behavior

### Auth

- `PUT` endpoints require authenticated owner or admin access
- `DELETE /api/auth/users/{id}` should invalidate active tokens

### Documents

- Only the owner or recruiter/admin should update or delete a document
- Deleting a job description should either block or invalidate related analyses

### Jobs

- Only the recruiter owner or admin should update or close a job
- Closed or archived jobs should not accept new applications

### Applications

- A candidate should not be able to apply to the same job twice unless the product explicitly allows re-application
- Each application should reference the exact CV version used at apply time
- Recruiters should only see applications for their own jobs

### Analysis

- `POST /start` should validate that the target job exists and that only applications for that job are analyzed
- `PUT /restart` should generate a fresh result timestamp and clear stale cached state
- `DELETE` should also remove related cache keys

---

## 5. System Workflow

1. A recruiter registers or logs in through the Auth Service.
2. A recruiter creates a job posting through the Job Service and can optionally upload a job description document through the Document Service.
3. The Document Service extracts and stores text content and metadata for both job descriptions and candidate CVs.
4. A candidate registers or logs in, browses open jobs, uploads or selects a CV, and applies to a specific job through the Application Service.
5. When the recruiter triggers analysis through the Dispatcher, the system automatically loads all applications for that job and their linked CV documents from the database.
6. The AI Analysis Service fetches the job description and candidate document content from the Document Service.
7. The LLM pipeline filters, scores, and ranks the candidates.
8. The ranked report is stored and returned to the recruiter.
9. If documents change later, the recruiter can reprocess documents or restart analysis using the `PUT` endpoints.
10. If outdated records are no longer needed, the recruiter or admin can clean them up using the `DELETE` endpoints.

---

## 6. Minimal Endpoint Set Required for Normal Operation

If the goal is a usable first production version, these endpoints are the minimum set that should exist:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/validate`
- `PUT /api/auth/users/{id}`
- `DELETE /api/auth/users/{id}`
- `POST /api/documents/upload`
- `GET /api/documents/{id}`
- `GET /api/documents/owner/{userId}`
- `GET /api/documents/content/{id}`
- `PUT /api/documents/{id}`
- `PUT /api/documents/{id}/reprocess`
- `DELETE /api/documents/{id}`
- `POST /api/jobs`
- `GET /api/jobs`
- `GET /api/jobs/{id}`
- `PUT /api/jobs/{id}`
- `PUT /api/jobs/{id}/status`
- `DELETE /api/jobs/{id}`
- `POST /api/applications`
- `GET /api/applications/job/{jobId}`
- `GET /api/applications/candidate/{candidateId}`
- `PUT /api/applications/{id}/status`
- `DELETE /api/applications/{id}`
- `POST /api/analysis/start`
- `GET /api/analysis/{analysisId}`
- `GET /api/analysis/report/{jobId}`
- `PUT /api/analysis/{analysisId}/restart`
- `DELETE /api/analysis/{analysisId}`

---

## 7. Implementation Notes

- Prefer soft delete for users and possibly for documents if audit history matters
- `Application` is the core entity that connects candidate, job, and CV
- A candidate should apply to a specific job, not be globally attached to every recruiter search
- Analysis status tracking is important because LLM processing is not always immediate
- Reprocess and restart endpoints are practical necessities, not just nice-to-have features
- If analysis history matters long-term, Redis alone may not be enough and a persistent store should be added

---

## 8. Development Protocol

All contributors should follow a strict TDD workflow:

1. **Red** - Write a failing test for the requirement
2. **Green** - Implement the minimal code needed to pass the test
3. **Refactor** - Improve naming, structure, and error handling without changing behavior

**Commit Convention**

- `test: [RED] <message>`
- `feat: [GREEN] <message>`
- `refactor: [REFACTOR] <message>`
