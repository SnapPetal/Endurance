package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.QuizEntity;
import com.thonbecker.endurance.type.QuizStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    List<QuizEntity> findByStatus(QuizStatus status);

    List<QuizEntity> findByStatusIn(List<QuizStatus> statuses);

    List<QuizEntity> findByTitleContainingIgnoreCase(String titlePart);
}
