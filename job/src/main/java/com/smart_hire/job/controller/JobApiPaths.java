package com.smart_hire.job.controller;

public final class JobApiPaths {

    public static final String BASE_PATH = "/api/jobs";
    public static final String JOB_BY_ID_PATH = "/{id}";
    public static final String JOBS_BY_RECRUITER_PATH = "/recruiter/{recruiterId}";
    public static final String JOB_STATUS_PATH = "/{id}/status";

    private JobApiPaths() {
    }
}
