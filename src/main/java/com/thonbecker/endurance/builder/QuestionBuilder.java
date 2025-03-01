package com.thonbecker.endurance.builder;

import com.thonbecker.endurance.type.Question;
import java.util.ArrayList;
import java.util.List;

public class QuestionBuilder {
    private Long id;
    private String questionText;
    private List<String> options = new ArrayList<>();
    private int correctOptionIndex;
    private int points = 1; // default value

    public QuestionBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public QuestionBuilder questionText(String questionText) {
        this.questionText = questionText;
        return this;
    }

    public QuestionBuilder addOption(String option) {
        this.options.add(option);
        return this;
    }

    public QuestionBuilder correctOptionIndex(int index) {
        this.correctOptionIndex = index;
        return this;
    }

    public QuestionBuilder points(int points) {
        this.points = points;
        return this;
    }

    public Question build() {
        return new Question(id, questionText, List.copyOf(options), correctOptionIndex, points);
    }
}
