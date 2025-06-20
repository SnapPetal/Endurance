package com.thonbecker.endurance.exception;

/**
 * Exception thrown when input validation fails.
 */
public class ValidationException extends QuizException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String message) {
        super("Validation error for field '" + field + "': " + message);
    }
}
