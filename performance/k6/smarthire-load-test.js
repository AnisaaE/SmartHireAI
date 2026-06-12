import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export const errorRate = new Rate('scenario_errors');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const RAMP_UP = __ENV.RAMP_UP || '10s';
const STEADY = __ENV.STEADY || '30s';
const RAMP_DOWN = __ENV.RAMP_DOWN || '10s';
const ANALYSIS_RATE = Number(__ENV.ANALYSIS_RATE || 2);
const THINK_TIME = Number(__ENV.THINK_TIME || 1);

export const options = {
  scenarios: {
    auth_login: {
      executor: 'ramping-vus',
      exec: 'authLoginScenario',
      startVUs: 0,
      stages: [
        { duration: RAMP_UP, target: 10 },
        { duration: STEADY, target: 20 },
        { duration: RAMP_DOWN, target: 0 },
      ],
      gracefulRampDown: '5s',
    },
    recruiter_reads: {
      executor: 'constant-vus',
      exec: 'recruiterReadScenario',
      vus: 12,
      duration: STEADY,
      startTime: '2s',
    },
    application_reads: {
      executor: 'constant-vus',
      exec: 'applicationReadScenario',
      vus: 10,
      duration: STEADY,
      startTime: '2s',
    },
    analysis_report: {
      executor: 'constant-vus',
      exec: 'analysisReportScenario',
      vus: 8,
      duration: STEADY,
      startTime: '2s',
    },
    analysis_start: {
      executor: 'constant-arrival-rate',
      exec: 'analysisStartScenario',
      rate: ANALYSIS_RATE,
      timeUnit: '1s',
      duration: STEADY,
      preAllocatedVUs: 4,
      maxVUs: 12,
      startTime: '2s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    checks: ['rate>0.95'],
    scenario_errors: ['rate<0.05'],
    'http_req_duration{scenario:auth_login}': ['p(95)<800'],
    'http_req_duration{scenario:recruiter_reads}': ['p(95)<900'],
    'http_req_duration{scenario:application_reads}': ['p(95)<900'],
    'http_req_duration{scenario:analysis_report}': ['p(95)<1200'],
    'http_req_duration{scenario:analysis_start}': ['p(95)<2000'],
  },
};

function jsonHeaders(token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return { headers };
}

function multipartHeaders(token) {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return { headers };
}

function parseJson(response) {
  try {
    return response.json();
  } catch (_) {
    return null;
  }
}

function expectStatus(response, allowedStatuses, label) {
  const ok = check(response, {
    [`${label} status is expected`]: (res) => allowedStatuses.includes(res.status),
  });
  errorRate.add(!ok);
  return ok;
}

function registerUser(user) {
  const response = http.post(
    `${BASE_URL}/api/auth/register`,
    JSON.stringify(user),
    jsonHeaders()
  );
  if (!expectStatus(response, [201], `register ${user.username}`)) {
    fail(`User registration failed for ${user.username}: ${response.status} ${response.body}`);
  }
}

function login(username, password) {
  const response = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username, password }),
    jsonHeaders()
  );
  if (!expectStatus(response, [200], `login ${username}`)) {
    fail(`Login failed for ${username}: ${response.status} ${response.body}`);
  }
  const payload = parseJson(response);
  if (!payload?.token) {
    fail(`Login token missing for ${username}`);
  }
  return payload.token;
}

function getAllUsers(token) {
  const response = http.get(`${BASE_URL}/api/auth/users`, jsonHeaders(token));
  if (!expectStatus(response, [200], 'get all users')) {
    fail(`Unable to read users: ${response.status} ${response.body}`);
  }
  return parseJson(response) || [];
}

function createJob(token, recruiterId, title) {
  const response = http.post(
    `${BASE_URL}/api/jobs`,
    JSON.stringify({
      recruiterId,
      title,
      description: 'Java Spring Boot microservices position with Redis, MongoDB and API integration.',
      location: 'Remote',
      employmentType: 'FULL_TIME',
    }),
    jsonHeaders(token)
  );
  if (!expectStatus(response, [201], 'create job')) {
    fail(`Job creation failed: ${response.status} ${response.body}`);
  }
}

function getRecruiterJobs(token, recruiterId) {
  const response = http.get(
    `${BASE_URL}/api/jobs/recruiter/${recruiterId}`,
    jsonHeaders(token)
  );
  if (!expectStatus(response, [200], 'get recruiter jobs')) {
    fail(`Unable to read recruiter jobs: ${response.status} ${response.body}`);
  }
  return parseJson(response) || [];
}

function getJobDetail(token, jobId) {
  const response = http.get(`${BASE_URL}/api/jobs/${jobId}`, jsonHeaders(token));
  if (!expectStatus(response, [200], 'get job detail')) {
    fail(`Unable to read job detail ${jobId}: ${response.status} ${response.body}`);
  }
  return parseJson(response);
}

function openJob(token, jobId, detail) {
  const response = http.put(
    `${BASE_URL}/api/jobs/${jobId}/status`,
    JSON.stringify({ status: 'OPEN' }),
    jsonHeaders(token)
  );
  if (!expectStatus(response, [200], 'open job')) {
    fail(`Unable to open job ${jobId}: ${response.status} ${response.body}`);
  }

  // Keep the original detail around if the API response shape changes.
  return parseJson(response) || detail;
}

function uploadCandidateCv(token, candidateId, candidateUsername) {
  const response = http.post(
    `${BASE_URL}/api/documents/upload`,
    {
      ownerId: String(candidateId),
      type: 'CV',
      title: `${candidateUsername} CV`,
      file: http.file(
        [
          'Senior Java developer with Spring Boot, REST API design, Redis, MongoDB, Docker, k6 and microservices.',
          'Built recruiter dashboards, authentication systems and candidate evaluation workflows.',
          'Experience with performance testing, observability and backend optimization.',
        ].join('\n'),
        `${candidateUsername}-cv.txt`,
        'text/plain'
      ),
    },
    multipartHeaders(token)
  );
  if (!expectStatus(response, [201], 'upload CV')) {
    fail(`CV upload failed: ${response.status} ${response.body}`);
  }
  const payload = parseJson(response);
  if (!payload?.id) {
    fail('Uploaded CV document id is missing');
  }
  return payload;
}

function createApplication(token, jobId, candidateId, cvDocumentId) {
  const response = http.post(
    `${BASE_URL}/api/applications`,
    JSON.stringify({ jobId, candidateId, cvDocumentId }),
    jsonHeaders(token)
  );
  if (!expectStatus(response, [201], 'create application')) {
    fail(`Application creation failed: ${response.status} ${response.body}`);
  }
}

function getCandidateApplications(token, candidateId) {
  const response = http.get(
    `${BASE_URL}/api/applications/candidate/${candidateId}`,
    jsonHeaders(token)
  );
  if (!expectStatus(response, [200], 'get candidate applications')) {
    fail(`Unable to read candidate applications: ${response.status} ${response.body}`);
  }
  return parseJson(response) || [];
}

function buildAnalysisPayload(jobDetail, applicationId, candidateId, cvDocumentId) {
  return {
    jobId: String(jobDetail.id),
    jobTitle: jobDetail.title,
    jobDescription: jobDetail.description,
    applications: [
      {
        applicationId,
        candidateId,
        cvDocumentId,
        candidateLabel: `Candidate #${candidateId}`,
      },
    ],
    configuration: {
      scoringWeights: {
        java: 0.35,
        spring: 0.30,
        microservices: 0.20,
        performance: 0.15,
      },
      evaluationCriteria: ['java', 'spring', 'microservices', 'performance'],
    },
  };
}

function startAnalysis(token, payload, label) {
  const response = http.post(
    `${BASE_URL}/api/analysis/start`,
    JSON.stringify(payload),
    jsonHeaders(token)
  );
  if (!expectStatus(response, [201], label || 'start analysis')) {
    fail(`Analysis start failed: ${response.status} ${response.body}`);
  }
  return parseJson(response);
}

export function setup() {
  const runId = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  const recruiter = {
    username: `perf_recruiter_${runId}`,
    email: `perf_recruiter_${runId}@smarthire.test`,
    password: 'Recruiter123!',
    role: 'RECRUITER',
  };
  const candidate = {
    username: `perf_candidate_${runId}`,
    email: `perf_candidate_${runId}@smarthire.test`,
    password: 'Candidate123!',
    role: 'CANDIDATE',
  };
  const jobTitle = `Performance Test Java Engineer ${runId}`;

  registerUser(recruiter);
  registerUser(candidate);

  const recruiterToken = login(recruiter.username, recruiter.password);
  const candidateToken = login(candidate.username, candidate.password);
  const users = getAllUsers(recruiterToken);

  const recruiterUser = users.find((user) => user.username === recruiter.username);
  const candidateUser = users.find((user) => user.username === candidate.username);

  if (!recruiterUser?.id || !candidateUser?.id) {
    fail('Unable to resolve seeded user ids from auth service');
  }

  createJob(recruiterToken, recruiterUser.id, jobTitle);
  const jobs = getRecruiterJobs(recruiterToken, recruiterUser.id);
  const createdJob = jobs.find((job) => job.title === jobTitle) || jobs[0];
  if (!createdJob?.id) {
    fail('Unable to resolve created job id from recruiter job list');
  }

  const jobDetail = getJobDetail(recruiterToken, createdJob.id);
  const openedJob = openJob(recruiterToken, createdJob.id, jobDetail);
  const cv = uploadCandidateCv(candidateToken, candidateUser.id, candidate.username);

  createApplication(candidateToken, createdJob.id, candidateUser.id, cv.id);
  const candidateApplications = getCandidateApplications(candidateToken, candidateUser.id);
  const createdApplication = candidateApplications.find((application) => application.jobId === createdJob.id);
  if (!createdApplication?.id) {
    fail('Unable to resolve created application id from candidate application list');
  }

  const baselineAnalysis = startAnalysis(
    recruiterToken,
    buildAnalysisPayload(openedJob || jobDetail, createdApplication.id, candidateUser.id, cv.id),
    'baseline analysis'
  );

  return {
    recruiter: {
      username: recruiter.username,
      password: recruiter.password,
      token: recruiterToken,
      id: recruiterUser.id,
    },
    candidate: {
      username: candidate.username,
      password: candidate.password,
      token: candidateToken,
      id: candidateUser.id,
    },
    job: {
      id: createdJob.id,
      title: jobTitle,
      description: (openedJob || jobDetail).description,
    },
    application: {
      id: createdApplication.id,
      cvDocumentId: cv.id,
    },
    analysis: {
      id: baselineAnalysis?.analysisId,
    },
  };
}

export function authLoginScenario(data) {
  const response = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      username: data.recruiter.username,
      password: data.recruiter.password,
    }),
    jsonHeaders()
  );
  expectStatus(response, [200], 'scenario login');

  const validation = http.get(
    `${BASE_URL}/api/auth/validate`,
    jsonHeaders(data.recruiter.token)
  );
  expectStatus(validation, [200], 'scenario validate token');

  sleep(THINK_TIME);
}

export function recruiterReadScenario(data) {
  const listResponse = http.get(`${BASE_URL}/api/jobs`, jsonHeaders(data.recruiter.token));
  expectStatus(listResponse, [200], 'list all jobs');

  const recruiterJobsResponse = http.get(
    `${BASE_URL}/api/jobs/recruiter/${data.recruiter.id}`,
    jsonHeaders(data.recruiter.token)
  );
  expectStatus(recruiterJobsResponse, [200], 'list recruiter jobs');

  const detailResponse = http.get(
    `${BASE_URL}/api/jobs/${data.job.id}`,
    jsonHeaders(data.recruiter.token)
  );
  expectStatus(detailResponse, [200], 'job detail read');

  sleep(THINK_TIME);
}

export function applicationReadScenario(data) {
  const byJobResponse = http.get(
    `${BASE_URL}/api/applications/job/${data.job.id}`,
    jsonHeaders(data.recruiter.token)
  );
  expectStatus(byJobResponse, [200], 'applications by job');

  const byCandidateResponse = http.get(
    `${BASE_URL}/api/applications/candidate/${data.candidate.id}`,
    jsonHeaders(data.candidate.token)
  );
  expectStatus(byCandidateResponse, [200], 'applications by candidate');

  const detailResponse = http.get(
    `${BASE_URL}/api/applications/${data.application.id}`,
    jsonHeaders(data.recruiter.token)
  );
  expectStatus(detailResponse, [200], 'application detail');

  sleep(THINK_TIME);
}

export function analysisReportScenario(data) {
  const reportResponse = http.get(
    `${BASE_URL}/api/analysis/report/${data.job.id}`,
    jsonHeaders(data.recruiter.token)
  );
  expectStatus(reportResponse, [200], 'analysis report');

  if (data.analysis.id) {
    const candidatesResponse = http.get(
      `${BASE_URL}/api/analysis/${data.analysis.id}/candidates`,
      jsonHeaders(data.recruiter.token)
    );
    expectStatus(candidatesResponse, [200], 'analysis candidates');
  }

  sleep(THINK_TIME);
}

export function analysisStartScenario(data) {
  startAnalysis(
    data.recruiter.token,
    buildAnalysisPayload(
      {
        id: data.job.id,
        title: data.job.title,
        description: data.job.description,
      },
      data.application.id,
      data.candidate.id,
      data.application.cvDocumentId
    ),
    'scenario analysis start'
  );

  sleep(THINK_TIME);
}

export function handleSummary(summary) {
  return {
    'performance/results/k6-summary.json': JSON.stringify(summary, null, 2),
  };
}
