package com.smart_hire.application.controller;

import com.smart_hire.application.service.ApplicationNotFoundException;
import com.smart_hire.application.service.DuplicateApplicationException;
import com.smart_hire.application.service.InvalidApplicationStatusException;
import com.smart_hire.application.service.InvalidCvDocumentException;
import com.smart_hire.application.service.JobUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ProblemDetail handleApplicationNotFound(ApplicationNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ProblemDetail handleDuplicateApplication(DuplicateApplicationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler({InvalidApplicationStatusException.class, InvalidCvDocumentException.class, JobUnavailableException.class})
    public ProblemDetail handleBusinessValidation(RuntimeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}
