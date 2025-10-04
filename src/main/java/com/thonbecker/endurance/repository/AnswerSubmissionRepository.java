package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.AnswerSubmissionEntity;
import com.thonbecker.endurance.entity.PlayerEntity;
import com.thonbecker.endurance.entity.QuestionEntity;
import com.thonbecker.endurance.entity.QuizEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerSubmissionRepository extends JpaRepository<AnswerSubmissionEntity, Long> {

    List<AnswerSubmissionEntity> findByQuiz(QuizEntity quiz);

    List<AnswerSubmissionEntity> findByPlayer(PlayerEntity player);

    List<AnswerSubmissionEntity> findByQuestion(QuestionEntity question);

    List<AnswerSubmissionEntity> findByQuizAndPlayer(QuizEntity quiz, PlayerEntity player);

    Optional<AnswerSubmissionEntity> findByQuizAndPlayerAndQuestion(
            QuizEntity quiz, PlayerEntity player, QuestionEntity question);

    boolean existsByQuizAndPlayerAndQuestion(
            QuizEntity quiz, PlayerEntity player, QuestionEntity question);

    @Query(
            "SELECT COUNT(a) FROM AnswerSubmissionEntity a WHERE a.quiz = :quiz AND a.question = :question AND a.player.id IN :playerIds")
    long countByQuizAndQuestionAndPlayerIdIn(
            @Param("quiz") QuizEntity quiz,
            @Param("question") QuestionEntity question,
            @Param("playerIds") List<String> playerIds);
}
