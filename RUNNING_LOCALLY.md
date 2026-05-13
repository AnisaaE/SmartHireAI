# Running SmartHireAI Locally

## Infrastructure

Start the databases first:

```powershell
docker compose up -d postgres mongo redis
```

This starts:

- PostgreSQL on `localhost:5432`
- MongoDB on `localhost:27017`
- Redis on `localhost:6379`

## Ollama

Make sure Ollama is already running locally and the model exists:

```powershell
ollama list
ollama pull llama3.2
```

The current runtime expects Ollama on `http://localhost:11434`.

## Document Service

```powershell
cd document
mvn spring-boot:run
```

The service starts on `http://localhost:8082`.

Upload a CV first and keep the returned `id`:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8082/api/documents/upload?ownerId=9001&type=cv&title=Alice%20CV" `
  -Form @{
    file = Get-Item "C:\path\to\alice-cv.txt"
  }
```

## AI Analysis Service

```powershell
cd ai_analysis
mvn spring-boot:run
```

The service starts on `http://localhost:8083`.

Runtime defaults:

- `ANALYSIS_ENGINE=llm`
- `ANALYSIS_STORAGE=redis`
- `OLLAMA_MODEL=llama3.2`
- `DOCUMENT_SERVICE_BASE_URL=http://localhost:8082`

Example overrides:

```powershell
$env:ANALYSIS_ENGINE="llm"
$env:ANALYSIS_STORAGE="redis"
$env:OLLAMA_MODEL="llama3.2"
$env:DOCUMENT_SERVICE_BASE_URL="http://localhost:8082"
```

## AI Smoke Test

```powershell
$body = @"
{
  "jobId": "job-77",
  "jobTitle": "Platform Engineer",
  "jobDescription": "Build resilient backend services with Java, Spring and distributed systems.",
  "applications": [
    {
      "applicationId": 401,
      "candidateId": 9001,
      "cvDocumentId": "<PUT_DOCUMENT_ID_HERE>",
      "candidateLabel": "Alice Johnson"
    }
  ],
  "configuration": {
    "scoringWeights": {
      "java": 0.4,
      "spring": 0.3,
      "distributed systems": 0.3
    },
    "evaluationCriteria": ["java", "spring", "distributed systems"]
  }
}
"@

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8083/api/analysis/start `
  -ContentType "application/json" `
  -Body $body
```

Then fetch the latest report for the job:

```powershell
Invoke-RestMethod http://localhost:8083/api/analysis/report/job-77
```
