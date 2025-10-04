package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.QuestionEntity;
import com.thonbecker.endurance.entity.QuizEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    List<QuestionEntity> findByQuiz(QuizEntity quiz);

    List<QuestionEntity> findByQuizOrderByQuestionOrderAsc(QuizEntity quiz);

    long countByQuiz(QuizEntity quiz);
}
