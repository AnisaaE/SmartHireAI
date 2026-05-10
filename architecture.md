Here is the comprehensive **Technical Design Document (TDD)** for your project. You can save this as `architecture.md` in your project root. It is written in professional technical English, which will help the AI (and potential employers) understand the system perfectly.

---

# 🏗️ SmartHireAI — Technical Specification & Architecture

## 1. System Overview

SmartHireAI is a microservices-based recruitment platform that uses Local LLMs (Ollama) to automate candidate screening. The system analyzes Job Descriptions and resumes (CVs) to rank the top 10 candidates for any given role.

### Tech Stack Summary

| Service | Technology | Database | Port |
| --- | --- | --- | --- |
| **Dispatcher** | Spring Cloud Gateway | — | 8080 |
| **Auth Service** | Spring Security 6, JWT | PostgreSQL | 8081 |
| **Document Service** | Spring AI, Apache Tika | MongoDB | 8082 |
| **AI Analysis** | Spring AI, Ollama (Llama3/Mistral) | Redis | 8083 |

---

## 2. Service Specifications

### 🔐 2.1. Auth Service (Identity & Access Management)

**Role:** The entry gate for security. Manages who can access the system and what they can do.

* **Key Features:**
* User Registration & Login.
* Role-Based Access Control (RBAC): `RECRUITER` (can post jobs), `CANDIDATE` (can upload CVs).
* JWT Generation and Claims validation.


* **Core Endpoints:**
* `POST /api/auth/register` — Create a new account.
* `POST /api/auth/login` — Exchange credentials for a JWT.
* `GET /api/auth/validate` — Internal check for the Gateway to verify tokens.



### 📄 2.2. Document Service (Content Extraction)

**Role:** Handles the "unstructured" part of the data—PDF files.

* **Key Features:**
* Multipart File Upload.
* **Text Extraction:** Utilizes Spring AI and Tika to convert PDF binaries into clean, searchable text.
* Storage of metadata and extracted text in a NoSQL environment for flexibility.


* **Core Endpoints:**
* `POST /api/documents/upload` — Upload a PDF (Job or CV).
* `GET /api/documents/owner/{userId}` — List all documents belonging to a user.
* `GET /api/documents/content/{id}` — Retrieve the raw extracted text for AI consumption.



### 🧠 2.3. AI Analysis Service (The Intelligent Engine)

**Role:** Processes natural language to make recruitment decisions.

* **Key Features:**
* **Multi-Agent Pipeline:**
1. **Filter Agent:** High-speed culling of irrelevant resumes.
2. **Scoring Agent:** Detailed 1-10 grading based on specific technical and soft skill criteria.
3. **Ranking Agent:** Final comparison to select the Top 10.


* **Result Caching:** Stores analysis results in Redis to avoid redundant LLM processing costs.


* **Core Endpoints:**
* `POST /api/analysis/start` — Trigger analysis for a Job ID against a set of CV IDs.
* `GET /api/analysis/report/{jobId}` — Fetch the ranked leaderboard and reasoning.



---

## 3. Data Models (Entities)

### PostgreSQL (Relational - Auth)

* **User Entity:** `Long id`, `String username`, `String password` (BCrypt), `String email`, `Role role`.

### MongoDB (Document-based - Document)

* **Document Entity:** `String id`, `String ownerId`, `DocType type` (JOB/CV), `String fileName`, `String rawTextContent`, `LocalDateTime createdAt`.

### Redis (Key-Value - Analysis)

* **AnalysisResult:** `jobId`, `Map<candidateId, score>`, `String reasoning`. (TTL: 24h).

---

## 4. System Workflow (The Journey of a Request)

1. **Preparation:** A Recruiter uploads a "Job Description" PDF and 100 "Candidate CV" PDFs via the `Document Service`.
2. **Processing:** The `Document Service` extracts text and stores it in MongoDB.
3. **Trigger:** The Recruiter clicks "Find Top 10". The request hits the `Dispatcher`.
4. **Coordination:** The `AI Analysis Service` fetches the text for all 101 documents from the `Document Service`.
5. **Intelligence:** The **Ollama** model processes the data through the 3-Agent pipeline.
6. **Response:** The system returns a structured JSON containing the Top 10 candidates with a "Reasoning" field explaining *why* they were chosen.

---

## 5. Implementation Protocol (Strict TDD)

All developers (and AI assistants) must follow the **Red-Green-Refactor** cycle:

1. **RED:** Write a test in `src/test/java` that reflects the requirement. The test must fail (or not compile).
2. **GREEN:** Write the absolute minimum code in `src/main/java` to satisfy the test.
3. **REFACTOR:** Optimize the code for SOLID principles. Ensure proper naming, remove duplication, and implement proper error handling.

**Commit Convention:**

* `test: [RED] <message>`
* `feat: [GREEN] <message>`
* `refactor: [REFACTOR] <message>`

---

### How to proceed:

Now that you have this file, you can start the project by telling the AI:
*"According to our `architecture.md`, let's begin with the **Auth Service**. First, generate the `pom.xml` with Spring Security, PostgreSQL, and JJWT dependencies. Then, write the first **RED** test for User Registration."*