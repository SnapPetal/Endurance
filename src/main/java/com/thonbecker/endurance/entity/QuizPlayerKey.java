package com.thonbecker.endurance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuizPlayerKey implements Serializable {

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "player_id")
    private String playerId;

    // Default constructor required by JPA
    public QuizPlayerKey() {}

    // Constructor with fields
    public QuizPlayerKey(Long quizId, String playerId) {
        this.quizId = quizId;
        this.playerId = playerId;
    }

    // Getters and setters
    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    // Equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizPlayerKey that = (QuizPlayerKey) o;
        return Objects.equals(quizId, that.quizId) && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, playerId);
    }
}
