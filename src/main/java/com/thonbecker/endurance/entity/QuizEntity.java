package com.thonbecker.endurance.entity;

import com.thonbecker.endurance.type.QuizStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz")
public class QuizEntity {

    @Id
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "time_per_question_in_seconds", nullable = false)
    private int timePerQuestionInSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuizStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionEntity> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizPlayerEntity> players = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<AnswerSubmissionEntity> submissions = new ArrayList<>();

    // Default constructor required by JPA
    public QuizEntity() {}

    // Constructor with fields
    public QuizEntity(Long id, String title, int timePerQuestionInSeconds, QuizStatus status) {
        this.id = id;
        this.title = title;
        this.timePerQuestionInSeconds = timePerQuestionInSeconds;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimePerQuestionInSeconds() {
        return timePerQuestionInSeconds;
    }

    public void setTimePerQuestionInSeconds(int timePerQuestionInSeconds) {
        this.timePerQuestionInSeconds = timePerQuestionInSeconds;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QuestionEntity> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionEntity> questions) {
        this.questions = questions;
    }

    public void addQuestion(QuestionEntity question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(QuestionEntity question) {
        questions.remove(question);
        question.setQuiz(null);
    }

    public List<QuizPlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<QuizPlayerEntity> players) {
        this.players = players;
    }

    public List<AnswerSubmissionEntity> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<AnswerSubmissionEntity> submissions) {
        this.submissions = submissions;
    }

    // Helper method to convert entity to domain model
    public com.thonbecker.endurance.type.Quiz toDomainModel() {
        List<com.thonbecker.endurance.type.Question> domainQuestions =
                questions.stream().map(QuestionEntity::toDomainModel).toList();

        return new com.thonbecker.endurance.type.Quiz(id, title, domainQuestions, timePerQuestionInSeconds, status);
    }

    // Helper method to create entity from domain model
    public static QuizEntity fromDomainModel(com.thonbecker.endurance.type.Quiz quiz) {
        QuizEntity entity = new QuizEntity(quiz.id(), quiz.title(), quiz.timePerQuestionInSeconds(), quiz.status());

        // Questions will be added separately to maintain proper bidirectional relationship

        return entity;
    }
}
