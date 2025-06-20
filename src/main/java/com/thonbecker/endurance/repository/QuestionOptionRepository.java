package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.QuestionEntity;
import com.thonbecker.endurance.entity.QuestionOptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOptionEntity, Long> {

    List<QuestionOptionEntity> findByQuestion(QuestionEntity question);

    List<QuestionOptionEntity> findByQuestionOrderByOptionOrderAsc(QuestionEntity question);

    void deleteByQuestion(QuestionEntity question);
}
