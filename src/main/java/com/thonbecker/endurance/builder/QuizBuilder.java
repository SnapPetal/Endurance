package com.thonbecker.endurance.builder;

import com.thonbecker.endurance.type.Question;
import com.thonbecker.endurance.type.Quiz;
import com.thonbecker.endurance.type.QuizStatus;

import java.util.ArrayList;
import java.util.List;

public class QuizBuilder {
    private Long id;
    private String title;
    private List<Question> questions = new ArrayList<>();
    private int timePerQuestionInSeconds = 30; // default value
    private QuizStatus status = QuizStatus.WAITING; // default value

    public QuizBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public QuizBuilder title(String title) {
        this.title = title;
        return this;
    }

    public QuizBuilder addQuestion(Question question) {
        this.questions.add(question);
        return this;
    }

    public QuizBuilder timePerQuestion(int seconds) {
        this.timePerQuestionInSeconds = seconds;
        return this;
    }

    public QuizBuilder status(QuizStatus status) {
        this.status = status;
        return this;
    }

    public Quiz build() {
        return new Quiz(id, title, List.copyOf(questions), timePerQuestionInSeconds, status);
    }
}
