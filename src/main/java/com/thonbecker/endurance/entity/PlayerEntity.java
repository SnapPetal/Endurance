package com.thonbecker.endurance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player", schema = "endurance")
public class PlayerEntity {

    @Id
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizPlayerEntity> quizzes = new ArrayList<>();

    // Default constructor required by JPA
    public PlayerEntity() {}

    // Constructor with fields
    public PlayerEntity(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QuizPlayerEntity> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(List<QuizPlayerEntity> quizzes) {
        this.quizzes = quizzes;
    }

    // Helper method to convert entity to domain model
    public com.thonbecker.endurance.type.Player toDomainModel(Long quizId) {
        // Find the QuizPlayerEntity for the specified quiz
        QuizPlayerEntity quizPlayer = quizzes.stream()
                .filter(qp -> qp.getQuiz().getId().equals(quizId))
                .findFirst()
                .orElse(null);

        int score = 0;
        boolean isReady = false;

        if (quizPlayer != null) {
            score = quizPlayer.getScore();
            isReady = quizPlayer.isReady();
        }

        return new com.thonbecker.endurance.type.Player(id, name, score, isReady);
    }

    // Helper method to create entity from domain model
    public static PlayerEntity fromDomainModel(com.thonbecker.endurance.type.Player player) {
        return new PlayerEntity(player.id(), player.name());
    }
}
