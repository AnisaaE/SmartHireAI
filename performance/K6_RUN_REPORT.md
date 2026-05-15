# SmartHireAI k6 Run Report

Run date: `2026-05-15`

## Environment

- Execution mode: Dockerized k6
- Base URL: `http://host.docker.internal:8080`
- Test script: `performance/k6/smarthire-load-test.js`
- Seed mode: automatic via `setup()`

## Applied load profile

- `auth_login`
  - ramping VUs
  - peak: 20 VUs
  - total active window: 50s
- `recruiter_reads`
  - 12 VUs
  - duration: 30s
- `application_reads`
  - 10 VUs
  - duration: 30s
- `analysis_report`
  - 8 VUs
  - duration: 30s
- `analysis_start`
  - 2 requests/second
  - duration: 30s

## Overall results

| Metric | Value |
| --- | --- |
| Total HTTP requests | 3497 |
| Request rate | 58.16 req/s |
| Total iterations | 1454 |
| Max virtual users | 54 |
| Check success rate | 100% |
| HTTP failure rate | 0% |
| Overall avg latency | 36.95 ms |
| Overall p95 latency | 111.95 ms |
| Overall max latency | 3066.08 ms |

## Scenario results

| Scenario | Avg | p95 | Max | Threshold | Result |
| --- | --- | --- | --- | --- | --- |
| Auth Login | 54.31 ms | 129.37 ms | 407.93 ms | p95 < 800 ms | Pass |
| Recruiter Reads | 17.89 ms | 61.82 ms | 245.05 ms | p95 < 900 ms | Pass |
| Application Reads | 20.04 ms | 63.80 ms | 502.17 ms | p95 < 900 ms | Pass |
| Analysis Report | 49.56 ms | 116.36 ms | 506.58 ms | p95 < 1200 ms | Pass |
| Analysis Start | 53.44 ms | 179.05 ms | 400.37 ms | p95 < 2000 ms | Pass |

## Interpretation

- All configured thresholds passed.
- No HTTP request failures were observed during the run.
- The system stayed responsive under concurrent read traffic and repeated AI analysis starts.
- The overall p95 latency remained well below the configured course/demo thresholds.
- The single high max latency value did not affect pass/fail status because p95 remained healthy.

## Course requirement mapping

This run supports the following requirement items:

- API & Back-end
  - API endpoints were exercised under concurrent traffic.
- Performance Tests
  - Load and stress-oriented requests were executed with k6.
- Analysis & Documentation
  - Results were exported as JSON and summarized as a written report.

## Artifacts

- JSON summary: `performance/results/k6-summary.json`
- Test script: `performance/k6/smarthire-load-test.js`
- Run instructions: `performance/README.md`

## Suggested next step

Repeat the same run after major backend changes and compare:

- request failure rate
- p95 latency
- AI analysis start latency
- recruiter/application read latency
