package com.thonbecker.endurance.type;

import java.util.Map;

public record QuizState(
        Long quizId,
        Question currentQuestion,
        int currentQuestionIndex,
        Map<String, Integer> playerScores,
        long questionStartTime) {}
