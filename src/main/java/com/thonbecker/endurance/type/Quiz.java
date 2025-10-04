package com.thonbecker.endurance.type;

import java.util.List;

public record Quiz(
        Long id,
        String title,
        List<Question> questions,
        int timePerQuestionInSeconds,
        QuizStatus status) {}
