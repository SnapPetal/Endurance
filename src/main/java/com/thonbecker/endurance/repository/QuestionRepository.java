package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.QuestionEntity;
import com.thonbecker.endurance.entity.QuizEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    List<QuestionEntity> findByQuiz(QuizEntity quiz);

    List<QuestionEntity> findByQuizOrderByQuestionOrderAsc(QuizEntity quiz);

    long countByQuiz(QuizEntity quiz);
}
