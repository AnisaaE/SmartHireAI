import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_RUN_ID = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
const CANDIDATE_COUNT = Number(__ENV.CANDIDATE_COUNT || 8);
const JOB_COUNT = Number(__ENV.JOB_COUNT || 2);
const STEADY = __ENV.STEADY || '45s';
const THINK_TIME_MIN = Number(__ENV.THINK_TIME_MIN || 0.5);
const THINK_TIME_MAX = Number(__ENV.THINK_TIME_MAX || 2);

export const unexpectedErrors = new Rate('unexpected_errors');
export const businessConflicts = new Counter('business_conflicts');
export const businessValidationFailures = new Counter('business_validation_failures');
export const successfulWrites = new Counter('successful_writes');

export const options = {
  scenarios: {
    recruiter_reads: {
      executor: 'constant-arrival-rate',
      exec: 'recruiterReads',
      rate: 18,
      timeUnit: '1s',
      duration: STEADY,
      preAllocatedVUs: 10,
      maxVUs: 24,
    },
    candidate_reads: {
      executor: 'constant-arrival-rate',
      exec: 'candidateReads',
      rate: 14,
      timeUnit: '1s',
      duration: STEADY,
      preAllocatedVUs: 10,
      maxVUs: 20,
      startTime: '2s',
    },
    apply_contention: {
      executor: 'constant-arrival-rate',
      exec: 'applyWithContention',
      rate: 5,
      timeUnit: '1s',
      duration: STEADY,
      preAllocatedVUs: 6,
      maxVUs: 16,
      startTime: '4s',
    },
    analysis_report_reads: {
      executor: 'constant-arrival-rate',
      exec: 'analysisReportReads',
      rate: 8,
      timeUnit: '1s',
      duration: STEADY,
      preAllocatedVUs: 6,
      maxVUs: 14,
      startTime: '4s',
    },
    analysis_starts: {
      executor: 'constant-arrival-rate',
      exec: 'analysisStarts',
      rate: 3,
      timeUnit: '1s',
      duration: STEADY,
      preAllocatedVUs: 4,
      maxVUs: 10,
      startTime: '6s',
    },
  },
  thresholds: {
    checks: ['rate>0.95'],
    http_req_failed: ['rate<0.08'],
    unexpected_errors: ['rate<0.03'],
    'http_req_duration{scenario:recruiter_reads}': ['p(95)<1000'],
    'http_req_duration{scenario:candidate_reads}': ['p(95)<1000'],
    'http_req_duration{scenario:apply_contention}': ['p(95)<1500'],
    'http_req_duration{scenario:analysis_report_reads}': ['p(95)<1500'],
    'http_req_duration{scenario:analysis_starts}': ['p(95)<2500'],
  },
};

function jsonParams(token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  return { headers };
}

function multipartParams(token) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  return { headers };
}

function parseJson(response) {
  try {
    return response.json();
  } catch (_) {
    return null;
  }
}

function think() {
  const duration = THINK_TIME_MIN + Math.random() * (THINK_TIME_MAX - THINK_TIME_MIN);
  sleep(duration);
}

function markUnexpectedIfNeeded(response, allowedStatuses) {
  const ok = allowedStatuses.includes(response.status);
  unexpectedErrors.add(!ok && response.status >= 500);
  return ok;
}

function must(response, allowedStatuses, label) {
  const ok = check(response, {
    [`${label} status expected`]: (res) => allowedStatuses.includes(res.status),
  });
  unexpectedErrors.add(!ok && response.status >= 500);
  if (!ok) {
    fail(`${label} failed with status ${response.status}: ${response.body}`);
  }
  return response;
}

function registerUser(user) {
  return must(
    http.post(`${BASE_URL}/api/auth/register`, JSON.stringify(user), jsonParams()),
    [201],
    `register ${user.username}`
  );
}

function login(username, password) {
  const response = must(
    http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ username, password }),
      jsonParams()
    ),
    [200],
    `login ${username}`
  );
  const payload = parseJson(response);
  if (!payload?.token) fail(`Missing token for ${username}`);
  return payload.token;
}

function allUsers(token) {
  return parseJson(
    must(http.get(`${BASE_URL}/api/auth/users`, jsonParams(token)), [200], 'list users')
  ) || [];
}

function createJob(token, recruiterId, title, location) {
  must(
    http.post(
      `${BASE_URL}/api/jobs`,
      JSON.stringify({
        recruiterId,
        title,
        description: `Production Java and Spring role focused on APIs, Docker, Redis, MongoDB and platform scale. ${title}`,
        location,
        employmentType: 'FULL_TIME',
      }),
      jsonParams(token)
    ),
    [201],
    `create job ${title}`
  );
}

function recruiterJobs(token, recruiterId) {
  return parseJson(
    must(
      http.get(`${BASE_URL}/api/jobs/recruiter/${recruiterId}`, jsonParams(token)),
      [200],
      'recruiter jobs'
    )
  ) || [];
}

function jobDetail(token, jobId) {
  return parseJson(
    must(http.get(`${BASE_URL}/api/jobs/${jobId}`, jsonParams(token)), [200], `job detail ${jobId}`)
  );
}

function openJob(token, jobId) {
  return parseJson(
    must(
      http.put(
        `${BASE_URL}/api/jobs/${jobId}/status`,
        JSON.stringify({ status: 'OPEN' }),
        jsonParams(token)
      ),
      [200],
      `open job ${jobId}`
    )
  );
}

function uploadCv(token, candidateId, username, skillTag) {
  const response = must(
    http.post(
      `${BASE_URL}/api/documents/upload`,
      {
        ownerId: String(candidateId),
        type: 'CV',
        title: `${username} CV`,
        file: http.file(
          [
            `${username} has experience in Java, Spring Boot, REST APIs and SQL.`,
            `Also worked with ${skillTag}, Docker, Redis, MongoDB and load testing.`,
            'Built dashboards, authentication flows and candidate ranking systems.',
          ].join('\n'),
          `${username}-cv.txt`,
          'text/plain'
        ),
        },
      multipartParams(token)
    ),
    [201],
    `upload CV ${username}`
  );
  return parseJson(response);
}

function applyToJob(token, jobId, candidateId, cvDocumentId) {
  const response = http.post(
    `${BASE_URL}/api/applications`,
    JSON.stringify({ jobId, candidateId, cvDocumentId }),
    jsonParams(token)
  );

  if (response.status === 201) {
    successfulWrites.add(1);
    check(response, { 'application created': (res) => res.status === 201 });
    return { kind: 'created', response };
  }
  if (response.status === 409) {
    businessConflicts.add(1);
    check(response, { 'duplicate application handled': (res) => res.status === 409 });
    return { kind: 'duplicate', response };
  }
  if (response.status === 400) {
    businessValidationFailures.add(1);
    check(response, { 'business validation handled': (res) => res.status === 400 });
    return { kind: 'validation', response };
  }

  markUnexpectedIfNeeded(response, [201, 400, 409]);
  fail(`Unexpected application response: ${response.status} ${response.body}`);
}

function applicationsByCandidate(token, candidateId) {
  return parseJson(
    must(
      http.get(`${BASE_URL}/api/applications/candidate/${candidateId}`, jsonParams(token)),
      [200],
      `applications by candidate ${candidateId}`
    )
  ) || [];
}

function applicationsByJob(token, jobId) {
  return parseJson(
    must(
      http.get(`${BASE_URL}/api/applications/job/${jobId}`, jsonParams(token)),
      [200],
      `applications by job ${jobId}`
    )
  ) || [];
}

function startAnalysis(token, payload, label) {
  const response = http.post(
    `${BASE_URL}/api/analysis/start`,
    JSON.stringify(payload),
    jsonParams(token)
  );
  if (!markUnexpectedIfNeeded(response, [201])) {
    fail(`${label} failed: ${response.status} ${response.body}`);
  }
  check(response, { [`${label} created`]: (res) => res.status === 201 });
  successfulWrites.add(1);
  return parseJson(response);
}

function analysisReport(token, jobId) {
  return parseJson(
    must(
      http.get(`${BASE_URL}/api/analysis/report/${jobId}`, jsonParams(token)),
      [200],
      `analysis report ${jobId}`
    )
  );
}

function buildAnalysisPayload(job, application) {
  return {
    jobId: String(job.id),
    jobTitle: job.title,
    jobDescription: job.description,
    applications: [
      {
        applicationId: application.id,
        candidateId: application.candidateId,
        cvDocumentId: application.cvDocumentId,
        candidateLabel: `Candidate #${application.candidateId}`,
      },
    ],
    configuration: {
      scoringWeights: {
        java: 0.3,
        spring: 0.25,
        docker: 0.2,
        performance: 0.15,
        redis: 0.1,
      },
      evaluationCriteria: ['java', 'spring', 'docker', 'performance', 'redis'],
    },
  };
}

function pick(items) {
  return items[Math.floor(Math.random() * items.length)];
}

export function setup() {
  const recruiterUser = {
    username: `real_recruiter_${TEST_RUN_ID}`,
    email: `real_recruiter_${TEST_RUN_ID}@smarthire.test`,
    password: 'Recruiter123!',
    role: 'RECRUITER',
  };
  registerUser(recruiterUser);
  const recruiterToken = login(recruiterUser.username, recruiterUser.password);
  const recruiterId = allUsers(recruiterToken).find((user) => user.username === recruiterUser.username)?.id;
  if (!recruiterId) fail('Recruiter id could not be resolved');

  const locations = ['Istanbul', 'Kocaeli', 'Ankara', 'Remote'];
  for (let i = 0; i < JOB_COUNT; i += 1) {
    createJob(recruiterToken, recruiterId, `Platform Engineer ${TEST_RUN_ID}-${i + 1}`, locations[i % locations.length]);
  }

  const seededJobs = recruiterJobs(recruiterToken, recruiterId)
    .filter((job) => String(job.title).includes(TEST_RUN_ID))
    .slice(0, JOB_COUNT)
    .map((job) => openJob(recruiterToken, job.id));

  const candidates = [];
  const skills = ['k6', 'observability', 'distributed systems', 'prompt engineering', 'kubernetes', 'caching'];

  for (let i = 0; i < CANDIDATE_COUNT; i += 1) {
    const user = {
      username: `real_candidate_${TEST_RUN_ID}_${i + 1}`,
      email: `real_candidate_${TEST_RUN_ID}_${i + 1}@smarthire.test`,
      password: 'Candidate123!',
      role: 'CANDIDATE',
    };
    registerUser(user);
    const token = login(user.username, user.password);
    const candidateId = allUsers(recruiterToken).find((entry) => entry.username === user.username)?.id;
    if (!candidateId) fail(`Candidate id could not be resolved for ${user.username}`);
    const cv = uploadCv(token, candidateId, user.username, skills[i % skills.length]);
    candidates.push({
      id: candidateId,
      username: user.username,
      password: user.password,
      token,
      cvDocumentId: cv.id,
    });
  }

  const seededApplications = [];
  for (let i = 0; i < Math.min(candidates.length, seededJobs.length * 2); i += 1) {
    const candidate = candidates[i];
    const job = seededJobs[i % seededJobs.length];
    applyToJob(candidate.token, job.id, candidate.id, candidate.cvDocumentId);
    const applications = applicationsByCandidate(candidate.token, candidate.id);
    const created = applications.find((entry) => entry.jobId === job.id);
    if (created) {
      seededApplications.push({
        ...created,
        cvDocumentId: candidate.cvDocumentId,
        jobId: job.id,
      });
    }
  }

  const analysisByJob = {};
  for (const job of seededJobs) {
    const application = seededApplications.find((entry) => entry.jobId === job.id);
    if (!application) continue;
    const result = startAnalysis(
      recruiterToken,
      buildAnalysisPayload(job, application),
      `baseline analysis job ${job.id}`
    );
    analysisByJob[job.id] = {
      analysisId: result.analysisId,
      applicationId: application.id,
      cvDocumentId: application.cvDocumentId,
      candidateId: application.candidateId,
    };
  }

  return {
    recruiter: {
      id: recruiterId,
      token: recruiterToken,
      username: recruiterUser.username,
      password: recruiterUser.password,
    },
    jobs: seededJobs,
    candidates,
    seededApplications,
    analysisByJob,
  };
}

export function recruiterReads(data) {
  const token = data.recruiter.token;
  const job = pick(data.jobs);
  must(http.get(`${BASE_URL}/api/jobs`, jsonParams(token)), [200], 'all jobs');
  must(http.get(`${BASE_URL}/api/jobs/recruiter/${data.recruiter.id}`, jsonParams(token)), [200], 'recruiter jobs read');
  must(http.get(`${BASE_URL}/api/jobs/${job.id}`, jsonParams(token)), [200], 'random job detail read');
  think();
}

export function candidateReads(data) {
  const candidate = pick(data.candidates);
  const apps = applicationsByCandidate(candidate.token, candidate.id);
  check(apps, { 'candidate applications payload exists': (value) => Array.isArray(value) });
  const job = pick(data.jobs);
  must(http.get(`${BASE_URL}/api/jobs/${job.id}`, jsonParams(candidate.token)), [200], 'candidate job detail read');
  think();
}

export function applyWithContention(data) {
  const candidate = pick(data.candidates);
  const job = pick(data.jobs);
  applyToJob(candidate.token, job.id, candidate.id, candidate.cvDocumentId);
  think();
}

export function analysisReportReads(data) {
  const job = pick(data.jobs);
  const report = analysisReport(data.recruiter.token, job.id);
  check(report, {
    'analysis report includes status': (value) => Boolean(value?.status),
  });
  think();
}

export function analysisStarts(data) {
  const job = pick(data.jobs);
  const application = data.seededApplications.find((entry) => entry.jobId === job.id) || pick(data.seededApplications);
  const fullJob = jobDetail(data.recruiter.token, job.id);
  startAnalysis(data.recruiter.token, buildAnalysisPayload(fullJob, application), `analysis start ${job.id}`);
  think();
}

export function handleSummary(summary) {
  return {
    'performance/results/k6-realistic-summary.json': JSON.stringify(summary, null, 2),
  };
}
