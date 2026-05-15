# SmartHireAI k6 Realistic Mixed-Load Report

Run date: `2026-05-15`

## Why this run is more realistic

This run is intentionally different from the baseline benchmark:

- multiple candidate accounts were seeded
- multiple jobs were created and opened
- recruiter reads, candidate reads, apply attempts, report reads, and analysis starts were mixed together
- repeated application attempts created real business-rule contention
- expected `409 Conflict` results were measured separately from actual system failures

## Load profile

| Scenario | Profile |
| --- | --- |
| Recruiter Reads | 18 iterations/s for 45s |
| Candidate Reads | 14 iterations/s for 45s |
| Apply Contention | 5 iterations/s for 45s |
| Analysis Report Reads | 8 iterations/s for 45s |
| Analysis Starts | 3 iterations/s for 45s |

## Overall results

| Metric | Value |
| --- | --- |
| Total iterations | 2059 |
| Max virtual users | 74 |
| Check success rate | 100% |
| Overall avg latency | 14.56 ms |
| Overall p95 latency | 30.92 ms |
| Overall max latency | 2314.37 ms |
| k6 `http_req_failed` | 4.82% |
| `unexpected_errors` | 0% |
| `business_conflicts` | 209 |
| Successful writes | 152 |

## Important interpretation

The `http_req_failed` metric in k6 counts all HTTP `4xx` and `5xx` responses.
Because this run intentionally included repeated apply attempts to the same jobs, duplicate applications produced expected `409 Conflict` responses.

That means:

- `http_req_failed = 4.82%` does **not** mean the platform broke
- the observed failures were expected business-rule collisions
- actual unexpected system failure rate remained `0%`

This is much closer to a real production story than a perfect `0%` failure line in a clean happy-path benchmark.

## Scenario latencies

| Scenario | Avg | p95 | Max |
| --- | --- | --- | --- |
| Recruiter Reads | 8.39 ms | 14.57 ms | 424.57 ms |
| Candidate Reads | 8.45 ms | 16.19 ms | 174.03 ms |
| Apply Contention | 30.82 ms | 75.94 ms | 992.73 ms |
| Analysis Report Reads | 31.56 ms | 77.34 ms | 623.07 ms |
| Analysis Starts | 19.79 ms | 48.21 ms | 361.00 ms |

## Observations

- The service stack stayed stable under mixed concurrent traffic.
- The heaviest business interaction was apply contention, which is expected because it creates write collisions.
- Analysis endpoints still remained comfortably below the configured p95 thresholds.
- k6 reported `insufficient VUs` warnings on some constant-arrival-rate scenarios, which suggests the configured `maxVUs` caps were reached during short spikes.

## What this means for the course requirement

This run is stronger evidence for the “Performans Testleri” requirement because it demonstrates:

- concurrent user behavior
- mixed read/write load
- controlled contention
- measurable non-zero failure-like outcomes
- distinction between business rejections and real system instability

## Recommended presentation wording

You can present the result like this:

"Temiz benchmark testinde hata oranı %0 çıktı; ancak bu senaryo kontrollü happy-path yüküydü. Daha gerçekçi mixed-load testinde tekrar eden başvuru denemeleri nedeniyle %4.82 oranında beklenen iş kuralı çakışması görüldü. Buna rağmen beklenmeyen sistem hatası %0 olarak ölçüldü ve servisler yük altında kararlı kaldı."

## Artifacts

- JSON summary: `performance/results/k6-realistic-summary.json`
- Script: `performance/k6/smarthire-realistic-load-test.js`
- Instructions: `performance/README.md`
