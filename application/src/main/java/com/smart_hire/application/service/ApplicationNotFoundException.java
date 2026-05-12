package com.smart_hire.application.service;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long applicationId) {
        super("Application not found: " + applicationId);
    }
}
