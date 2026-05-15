SMARTHIREAI
Advanced Java Applications
Project Overview and Technical Report

1. PROJECT IDENTITY
Project Name              : SmartHireAI
Course                    : Advanced Java Applications
Department                : Information Systems Engineering
Faculty                   : Faculty of Technology
University                : Kocaeli University
Course Instructor         : Dr. Samet Diri
Architecture Style        : Microservice-based distributed system
Repository Name           : SmartHireAI
Group Number              : 22

2. TEAM INFORMATION

Team Member 1
Full Name                 : Nalan Kara
Student ID                : 231307060
Commit Username           : nalankaraa
Responsibility Area       : System architecture, authentication, document service, AI analysis service, Docker, and technical documentation

Team Member 2
Full Name                 : Anisa Nezhmi Emin
Student ID                : 221307118
Commit Username           : AnisaaE
Responsibility Area       : Job postings, application workflows, gateway integration, user interface, and dashboard flows

3. PROJECT DESCRIPTION
SmartHireAI is a platform developed to digitalize the recruitment process and enhance candidate evaluation
with AI-supported analysis. The system allows recruiters to create job postings, candidates to upload resumes,
apply for positions, and receive evaluation results based on automated matching between job requirements and CV content.

The main focus of the project is to reduce manual pre-screening effort and help recruiters identify suitable
candidates more efficiently. For this reason, document processing, user authentication, application management,
and candidate-job matching have been designed as separate but connected services.

4. PROJECT OBJECTIVE
The project was developed with the following objectives:

- To centralize candidate, job, and document data that are typically scattered across different tools
- To process CVs and job descriptions automatically and turn them into analyzable content
- To evaluate candidate-job compatibility using an AI-supported scoring mechanism
- To build a modular, extensible, and maintainable Java-based system
- To combine security, data management, inter-service communication, and testing practices within one integrated project

5. GENERAL SYSTEM SUMMARY
SmartHireAI is a microservice-based application that combines multiple services and multiple persistence layers.
Requests from end users are first received by the dispatcher service and then routed to the relevant backend service.
Authentication is handled with a JWT-based flow. Relational data such as users, jobs, and applications are stored in
PostgreSQL, while documents are managed in MongoDB. Analysis results are stored in Redis for fast access and retrieval.

The system is structured so that it can run both in a local development environment and in a Docker-based environment.
This makes the project reproducible and convenient for development, testing, and presentation purposes.

6. ARCHITECTURE
The system consists of the following services, each designed with a clearly separated responsibility:

6.1. Dispatcher Service
Port                      : 8080
Purpose                   : Serves as the public entry point of the platform
Responsibilities          :
- Routing incoming requests to the correct microservice
- Hiding internal service topology from clients
- Supporting authentication-related request flows
- Providing a centralized API access layer

6.2. Auth Service
Port                      : 8081
Persistence Layer         : PostgreSQL
Purpose                   : Handles user registration, login, token generation, and user management
Core Functions            :
- Registration and login
- JWT token creation and validation
- Role-based user separation
- User profile management

6.3. Document Service
Port                      : 8082
Persistence Layer         : MongoDB
Purpose                   : Handles resume and document upload, text extraction, and metadata management
Core Functions            :
- File upload
- Document metadata storage
- Content extraction and persistence
- Active CV retrieval for candidates

6.4. AI Analysis Service
Port                      : 8083
Persistence Layer         : Redis
Integration               : Ollama / heuristic analysis flow
Purpose                   : Compares submitted applications with job requirements and generates suitability scores
Core Functions            :
- Starting analysis jobs
- Scoring applications
- Generating result reports
- Storing reusable analysis outputs

6.5. Job Service
Port                      : 8084
Persistence Layer         : PostgreSQL
Purpose                   : Manages job creation, update, listing, and status handling

6.6. Application Service
Port                      : 8085
Persistence Layer         : PostgreSQL
Purpose                   : Records candidate applications and tracks the application lifecycle

6.7. Frontend
Technology                : React + Vite
Purpose                   : Provides the user-facing interface of the platform

7. TECHNOLOGIES USED
- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- Spring Data JPA
- Spring Data MongoDB
- Spring Data Redis
- JWT
- PostgreSQL
- MongoDB
- Redis
- Ollama
- Docker
- Docker Compose
- Maven
- React
- Vite
- k6

8. DATA MANAGEMENT
Different storage technologies are used for different categories of data:

- PostgreSQL:
  Stores relational data such as users, job postings, and applications.

- MongoDB:
  Stores resume metadata and document-related content.

- Redis:
  Stores AI analysis outputs for fast lookup and retrieval.

This approach allows each data type to be stored in a technology that best fits its structure,
which improves both performance and architectural flexibility.

9. SYSTEM WORKFLOW
The main workflow of SmartHireAI is as follows:

1. A user registers or logs into the system.
2. A recruiter creates a new job posting.
3. A candidate uploads a CV.
4. The candidate applies for a selected job.
5. The Application service validates the relationship between the candidate, the job, and the uploaded CV.
6. The AI Analysis service compares the job description with the candidate's application data.
7. The system generates scored and ranked results.
8. The recruiter views job, application, and analysis summaries through the dashboard.

10. USER ROLES
The platform is designed around two main user roles:

- Recruiter:
  Creates job postings, reviews incoming applications, and examines AI analysis results.

- Candidate:
  Registers in the system, uploads a CV, applies to job postings, and tracks application history.

11. RESPONSIBILITY DISTRIBUTION BY MODULE
The internal team workflow was organized according to project modules.

Member 1 - Nalan Kara
- Development of the Auth service
- Development of the Document service
- Core design of the AI Analysis service
- Docker and compose configuration
- Architecture and technical documentation

Member 2 - Anisa Nezhmi Emin
- Development of the Job service
- Development of the Application service
- Implementation of dispatcher/gateway request flows
- Frontend screens and dashboard structure
- Testing and refinement of UI workflows

Shared Responsibilities
- Inter-service integration
- Error handling and system validation
- Load testing and final reporting
- Presentation preparation

12. TESTING AND VALIDATION
Validation activities were carried out at multiple levels during development.

12.1. Unit and Service Tests
Test classes were created at service and controller level to verify core business rules,
error conditions, and expected API behavior. Test coverage is especially visible in the
auth, job, application, and dispatcher layers.

12.2. API Verification
Service endpoints were verified through direct API calls during development.
This made it possible to observe data validation, status codes, connected service flows,
and dashboard behavior in a structured way.

12.3. Performance and Load Testing
k6-based performance testing was prepared for the project. All related test assets are stored
under the `performance` directory. Two different load profiles were created:

- Baseline Load Test:
  Designed to measure the system's response behavior in a controlled and clean environment.

- Realistic Mixed-Load Test:
  Designed to simulate more realistic user behavior through multiple candidates, multiple jobs,
  concurrent read-write operations, and repeated application attempts.

13. K6 TEST RESULTS
The summary results obtained from the performance test scenarios are presented below.

13.1. Baseline Test Result
This scenario measured the system in a controlled environment.

- Total HTTP requests       : 3497
- Request rate              : 58.16 req/s
- Total iterations          : 1454
- Maximum virtual users     : 54
- Success rate              : 100%
- HTTP failure rate         : 0%
- Average latency           : 36.95 ms
- p95 latency               : 111.95 ms

Scenario-based p95 results:
- Auth Login                : 129.37 ms
- Recruiter Reads           : 61.82 ms
- Application Reads         : 63.80 ms
- Analysis Report           : 116.36 ms
- Analysis Start            : 179.05 ms

13.2. Realistic Mixed-Load Test Result
This scenario included repeated application attempts and concurrent user behavior.

- Total iterations          : 2059
- Maximum virtual users     : 74
- Success rate              : 100%
- Average latency           : 14.56 ms
- p95 latency               : 30.92 ms
- Overall k6 failure rate   : 4.82%
- Unexpected system error rate : 0%
- Business-rule conflict count : 209
- Successful write operations  : 152

The 4.82% value observed in this test does not indicate system instability by itself.
Most of this percentage comes from expected business-rule collisions such as repeated applications
to the same job posting. Despite that, no unexpected system failure was observed. This shows that
the application was able to enforce business rules correctly while remaining stable under load.

14. LOCAL EXECUTION SUMMARY
The typical order for running the project locally is as follows:

1. Start the infrastructure services with Docker
2. Run the Auth service
3. Run the Document service
4. Run the AI Analysis service
5. Run the Job service
6. Run the Application service
7. Run the Dispatcher service
8. Start the Frontend application

Service ports:
- Dispatcher              : 8080
- Auth                    : 8081
- Document                : 8082
- AI Analysis             : 8083
- Job                     : 8084
- Application             : 8085
- Frontend                : 5173

15. FUTURE IMPROVEMENT AREAS
The project currently delivers the main workflows successfully and provides a strong technical foundation.
Potential future improvements include:

- Adding mobile client support
- Expanding dashboard reporting capabilities
- Integrating a more advanced real LLM-based analysis flow
- Improving observability and structured logging
- Moving authentication and token handling toward a production-grade implementation

16. CONCLUSION
SmartHireAI is a comprehensive software project that digitalizes the recruitment process, manages candidate
and job data through service-based architecture, and combines document processing with AI-supported analysis.
When evaluated together with its microservice architecture, multi-database approach, security layer,
dashboard functionality, and performance testing, the project presents a complete and technically coherent solution.
