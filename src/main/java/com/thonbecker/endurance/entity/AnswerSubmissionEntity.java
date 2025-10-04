package com.thonbecker.endurance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "answer_submission", schema = "endurance")
public class AnswerSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "selected_option", nullable = false)
    private int selectedOption;

    @Column(name = "submission_time", nullable = false)
    private long submissionTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor required by JPA
    public AnswerSubmissionEntity() {}

    // Constructor with fields
    public AnswerSubmissionEntity(
            PlayerEntity player,
            QuizEntity quiz,
            QuestionEntity question,
            int selectedOption,
            long submissionTime) {
        this.player = player;
        this.quiz = quiz;
        this.question = question;
        this.selectedOption = selectedOption;
        this.submissionTime = submissionTime;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public QuizEntity getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizEntity quiz) {
        this.quiz = quiz;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionEntity question) {
        this.question = question;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(long submissionTime) {
        this.submissionTime = submissionTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method to convert entity to domain model
    public com.thonbecker.endurance.type.AnswerSubmission toDomainModel() {
        return new com.thonbecker.endurance.type.AnswerSubmission(
                player.getId(), quiz.getId(), question.getId(), selectedOption, submissionTime);
    }

    // Helper method to create entity from domain model
    public static AnswerSubmissionEntity fromDomainModel(
            com.thonbecker.endurance.type.AnswerSubmission submission,
            PlayerEntity player,
            QuizEntity quiz,
            QuestionEntity question) {

        return new AnswerSubmissionEntity(
                player, quiz, question, submission.selectedOption(), submission.submissionTime());
    }
}
