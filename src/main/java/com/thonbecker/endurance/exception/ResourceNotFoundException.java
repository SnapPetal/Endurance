package com.thonbecker.endurance.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends QuizException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with ID " + resourceId + " not found");
    }

    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(resourceType + " with ID " + resourceId + " not found");
    }
}
