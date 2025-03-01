package com.thonbecker.endurance.type;

public record AnswerSubmission(String playerId, Long questionId, int selectedOption, long submissionTime) {}
