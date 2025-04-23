package com.thonbecker.endurance.type;

public record AnswerSubmission(
        String playerId, Long quizId, Long questionId, int selectedOption, long submissionTime) {}
