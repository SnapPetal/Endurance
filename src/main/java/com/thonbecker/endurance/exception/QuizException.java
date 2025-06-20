package com.thonbecker.endurance.exception;

/**
 * Base exception class for quiz-related exceptions.
 */
public class QuizException extends RuntimeException {
    public QuizException(String message) {
        super(message);
    }
}
