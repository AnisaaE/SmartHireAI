package com.smart_hire.application.service;

public class InvalidCvDocumentException extends RuntimeException {

    public InvalidCvDocumentException(String message) {
        super(message);
    }
}
