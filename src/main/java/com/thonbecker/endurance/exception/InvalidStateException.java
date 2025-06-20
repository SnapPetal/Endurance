package com.thonbecker.endurance.exception;

import com.thonbecker.endurance.type.QuizStatus;

/**
 * Exception thrown when an operation is attempted on a quiz that is not in the correct state.
 */
public class InvalidStateException extends QuizException {
    public InvalidStateException(String message) {
        super(message);
    }

    public InvalidStateException(QuizStatus currentStatus, QuizStatus requiredStatus) {
        super("Invalid quiz state: current status is " + currentStatus + ", but operation requires status "
                + requiredStatus);
    }

    public InvalidStateException(QuizStatus currentStatus, QuizStatus... allowedStatuses) {
        super("Invalid quiz state: current status is " + currentStatus + ", but operation requires one of "
                + String.join(
                        ", ",
                        java.util.Arrays.stream(allowedStatuses).map(Enum::name).toArray(String[]::new)));
    }
}
