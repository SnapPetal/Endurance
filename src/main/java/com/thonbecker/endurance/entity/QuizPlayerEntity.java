package com.thonbecker.endurance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_player", schema = "endurance")
public class QuizPlayerEntity {

    @EmbeddedId
    private QuizPlayerKey id;

    @ManyToOne
    @MapsId("quizId")
    @JoinColumn(name = "quiz_id")
    private QuizEntity quiz;

    @ManyToOne
    @MapsId("playerId")
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "is_ready", nullable = false)
    private boolean ready;

    // Default constructor required by JPA
    public QuizPlayerEntity() {}

    // Constructor with fields
    public QuizPlayerEntity(QuizEntity quiz, PlayerEntity player) {
        this.quiz = quiz;
        this.player = player;
        this.id = new QuizPlayerKey(quiz.getId(), player.getId());
        this.score = 0;
        this.ready = false;
    }

    // Getters and setters
    public QuizPlayerKey getId() {
        return id;
    }

    public void setId(QuizPlayerKey id) {
        this.id = id;
    }

    public QuizEntity getQuiz() {
        return quiz;
    }

    public void setQuiz(QuizEntity quiz) {
        this.quiz = quiz;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
