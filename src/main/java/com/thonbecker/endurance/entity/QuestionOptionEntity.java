package com.thonbecker.endurance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_option")
public class QuestionOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    // Default constructor required by JPA
    public QuestionOptionEntity() {}

    // Constructor with fields
    public QuestionOptionEntity(String optionText, int optionOrder) {
        this.optionText = optionText;
        this.optionOrder = optionOrder;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public void setQuestion(QuestionEntity question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public int getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(int optionOrder) {
        this.optionOrder = optionOrder;
    }
}
