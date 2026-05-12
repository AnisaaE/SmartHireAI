package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisNotFoundException;
import com.smart_hire.ai_analysis.service.AnalysisReferenceNotFoundException;
import com.smart_hire.ai_analysis.service.AnalysisReferenceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AnalysisExceptionHandler {

    @ExceptionHandler(AnalysisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleAnalysisNotFound(AnalysisNotFoundException ex) {
        return new ApiErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AnalysisReferenceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleReferenceNotFound(AnalysisReferenceNotFoundException ex) {
        return new ApiErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AnalysisReferenceValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleReferenceValidation(AnalysisReferenceValidationException ex) {
        return new ApiErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        return new ApiErrorResponse(message);
    }
}
