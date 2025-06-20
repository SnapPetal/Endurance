package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.QuizEntity;
import com.thonbecker.endurance.type.QuizStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    List<QuizEntity> findByStatus(QuizStatus status);

    List<QuizEntity> findByTitleContainingIgnoreCase(String titlePart);
}
