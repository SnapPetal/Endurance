package com.thonbecker.endurance.repository;

import com.thonbecker.endurance.entity.QuestionEntity;
import com.thonbecker.endurance.entity.QuestionOptionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOptionEntity, Long> {

    List<QuestionOptionEntity> findByQuestion(QuestionEntity question);

    List<QuestionOptionEntity> findByQuestionOrderByOptionOrderAsc(QuestionEntity question);

    void deleteByQuestion(QuestionEntity question);
}
