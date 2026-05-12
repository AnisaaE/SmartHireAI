package com.smart_hire.job.controller;

import com.smart_hire.job.service.InvalidJobStatusException;
import com.smart_hire.job.service.InvalidRecruiterException;
import com.smart_hire.job.service.JobDeletionConflictException;
import com.smart_hire.job.service.JobNotFoundException;
import com.smart_hire.job.service.RecruiterNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JobExceptionHandler {

    @ExceptionHandler(JobNotFoundException.class)
    public ProblemDetail handleJobNotFound(JobNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(RecruiterNotFoundException.class)
    public ProblemDetail handleRecruiterNotFound(RecruiterNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidJobStatusException.class)
    public ProblemDetail handleInvalidStatus(InvalidJobStatusException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidRecruiterException.class)
    public ProblemDetail handleInvalidRecruiter(InvalidRecruiterException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(JobDeletionConflictException.class)
    public ProblemDetail handleDeletionConflict(JobDeletionConflictException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}
