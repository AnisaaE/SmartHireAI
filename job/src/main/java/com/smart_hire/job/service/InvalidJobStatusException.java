package com.smart_hire.job.service;

public class InvalidJobStatusException extends RuntimeException {

    public InvalidJobStatusException(String status) {
        super("Invalid job status: " + status);
    }
}
