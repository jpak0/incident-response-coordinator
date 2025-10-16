package com.incidentresponse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all exceptions.
 * Used by all controllers to handle exceptions and return appropriate responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles IncidentNotFoundException.
     * Returns 404 Not Found response.
     * @param ex the exception to handle
     * @return the response entity
     */
    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIncidentNotFound(IncidentNotFoundException ex) {
        // Ex). "Incident not found with ID: 123"
        // Creates error response object with 404 Not Found status and message.
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        // Returns 404 Not Found response.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles IllegalStateException.
     * Returns 400 Bad Request response.
     * @param ex the exception to handle
     * @return the response entity
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        // Ex). "Incident is already in the INVESTIGATING state"
        // Creates error response object with 400 Bad Request status and message.
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        // Ex). "Incident is already in the INVESTIGATING state"
        // Returns 400 Bad Request response.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles MethodArgumentNotValidException.
     * Returns 400 Bad Request response.
     * @param ex the exception to handle
     * @return the response entity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Ex). "Title is required"
    // Creates error response object with 400 Bad Request status and message.   
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Ex). "Title is required"
        // Creates error response object with 400 Bad Request status and message.
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Ex). "Title is required"
        // Returns 400 Bad Request response.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    /**
     * Handles Exception.
     * Returns 500 Internal Server Error response.
     * @param ex the exception to handle
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    // Ex). "An unexpected error occurred: java.lang.NullPointerException"
    // Creates error response object with 500 Internal Server Error status and message.
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Ex). "An unexpected error occurred: java.lang.NullPointerException"
        // Creates error response object with 500 Internal Server Error status and message.
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred: " + ex.getMessage(),
            LocalDateTime.now()
        );

        // Ex). "An unexpected error occurred: java.lang.NullPointerException"
        // Creates error response object with 500 Internal Server Error status and message.
        // Ex). "An unexpected error occurred: java.lang.NullPointerException"
        // Returns 500 Internal Server Error response.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Error response object.
     * Used to return error responses to the client.
     * @param status the status code
     * @param message the error message
     * @param timestamp the timestamp of the error
     */
    record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
}
