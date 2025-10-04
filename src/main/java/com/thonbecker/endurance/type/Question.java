package com.thonbecker.endurance.type;

import java.util.List;

public record Question(
        Long id, String questionText, List<String> options, int correctOptionIndex, int points) {}
