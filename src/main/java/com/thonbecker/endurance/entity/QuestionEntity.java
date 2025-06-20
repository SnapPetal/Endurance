package com.thonbecker.endurance.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "question")
public class QuestionEntity {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "correct_option_index", nullable = false)
    private int correctOptionIndex;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOptionEntity> options = new ArrayList<>();

    // Default constructor required by JPA
    public QuestionEntity() {}

    // Constructor with fields
    public QuestionEntity(Long id, String questionText, int correctOptionIndex, int points, int questionOrder) {
        this.id = id;
        this.questionText = questionText;
        this.correctOptionIndex = correctOptionIndex;
        this.points = points;
        this.questionOrder = questionOrder;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuizEntity getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizEntity quiz) {
        this.quiz = quiz;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    public List<QuestionOptionEntity> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionEntity> options) {
        this.options = options;
    }

    public void addOption(QuestionOptionEntity option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(QuestionOptionEntity option) {
        options.remove(option);
        option.setQuestion(null);
    }

    // Helper method to convert entity to domain model
    public com.thonbecker.endurance.type.Question toDomainModel() {
        List<String> optionTexts = options.stream()
                .sorted((o1, o2) -> Integer.compare(o1.getOptionOrder(), o2.getOptionOrder()))
                .map(QuestionOptionEntity::getOptionText)
                .collect(Collectors.toList());

        return new com.thonbecker.endurance.type.Question(id, questionText, optionTexts, correctOptionIndex, points);
    }

    // Helper method to create entity from domain model
    public static QuestionEntity fromDomainModel(com.thonbecker.endurance.type.Question question, int order) {
        QuestionEntity entity = new QuestionEntity(
                question.id(), question.questionText(), question.correctOptionIndex(), question.points(), order);

        // Options will be added separately to maintain proper bidirectional relationship

        return entity;
    }
}
