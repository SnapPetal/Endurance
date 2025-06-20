package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.PlayerEntity;
import com.thonbecker.endurance.entity.QuizEntity;
import com.thonbecker.endurance.entity.QuizPlayerEntity;
import com.thonbecker.endurance.entity.QuizPlayerKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizPlayerRepository extends JpaRepository<QuizPlayerEntity, QuizPlayerKey> {

    List<QuizPlayerEntity> findByQuiz(QuizEntity quiz);

    List<QuizPlayerEntity> findByPlayer(PlayerEntity player);

    Optional<QuizPlayerEntity> findByQuizAndPlayer(QuizEntity quiz, PlayerEntity player);

    boolean existsByQuizAndPlayer(QuizEntity quiz, PlayerEntity player);

    void deleteByQuizAndPlayer(QuizEntity quiz, PlayerEntity player);
}
