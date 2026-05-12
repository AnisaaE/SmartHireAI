package com.smart_hire.application.service;

public class InvalidApplicationStatusException extends RuntimeException {

    public InvalidApplicationStatusException(String status) {
        super("Invalid application status: " + status);
    }
}
