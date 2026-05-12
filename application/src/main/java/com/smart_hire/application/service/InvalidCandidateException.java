package com.smart_hire.application.service;

public class InvalidCandidateException extends RuntimeException {

    public InvalidCandidateException(String message) {
        super(message);
    }
}
