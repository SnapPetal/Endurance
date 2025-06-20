package com.thonbecker.endurance.service;

import com.thonbecker.endurance.entity.*;
import com.thonbecker.endurance.exception.InvalidStateException;
import com.thonbecker.endurance.exception.ResourceNotFoundException;
import com.thonbecker.endurance.exception.ValidationException;
import com.thonbecker.endurance.repository.*;
import com.thonbecker.endurance.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private TriviaQuestionGenerator questionGenerator;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private QuizPlayerRepository quizPlayerRepository;

    @Mock
    private AnswerSubmissionRepository answerSubmissionRepository;

    @InjectMocks
    private QuizService quizService;

    private QuizEntity quizEntity;
    private QuestionEntity questionEntity;
    private PlayerEntity playerEntity;
    private QuizPlayerEntity quizPlayerEntity;
    private List<QuestionOptionEntity> options;

    @BeforeEach
    void setUp() {
        // Set up test data
        quizEntity = new QuizEntity(1L, "Test Quiz", 30, QuizStatus.CREATED);
        
        questionEntity = new QuestionEntity();
        questionEntity.setId(1L);
        questionEntity.setQuestionText("Test Question");
        questionEntity.setCorrectOptionIndex(0);
        questionEntity.setPoints(10);
        questionEntity.setQuiz(quizEntity);
        
        options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            QuestionOptionEntity option = new QuestionOptionEntity("Option " + i, i);
            option.setQuestion(questionEntity);
            options.add(option);
        }
        questionEntity.setOptions(options);
        
        List<QuestionEntity> questions = new ArrayList<>();
        questions.add(questionEntity);
        quizEntity.setQuestions(questions);
        
        playerEntity = new PlayerEntity("player1", "Test Player");
        
        quizPlayerEntity = new QuizPlayerEntity(quizEntity, playerEntity);
        quizPlayerEntity.setScore(0);
        quizPlayerEntity.setReady(true);
    }

    @Test
    void startQuiz_Success() {
        // Arrange
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quizEntity));
        when(questionRepository.findByQuizOrderByQuestionOrderAsc(quizEntity))
                .thenReturn(Collections.singletonList(questionEntity));
        when(quizPlayerRepository.findByQuiz(quizEntity))
                .thenReturn(Collections.singletonList(quizPlayerEntity));
        
        // Act
        QuizState result = quizService.startQuiz(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.quizId());
        assertEquals(0, result.currentQuestionIndex());
        assertEquals(1, result.playerScores().size());
        verify(quizRepository).save(quizEntity);
        assertEquals(QuizStatus.IN_PROGRESS, quizEntity.getStatus());
    }

    @Test
    void startQuiz_QuizNotFound() {
        // Arrange
        when(quizRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> quizService.startQuiz(1L));
    }

    @Test
    void startQuiz_InvalidState() {
        // Arrange
        quizEntity.setStatus(QuizStatus.IN_PROGRESS);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quizEntity));
        
        // Act & Assert
        assertThrows(InvalidStateException.class, () -> quizService.startQuiz(1L));
    }

    @Test
    void pauseQuiz_Success() {
        // Arrange
        quizEntity.setStatus(QuizStatus.IN_PROGRESS);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quizEntity));
        
        // Set up quiz state in the service
        Map<Long, QuizState> quizStates = new HashMap<>();
        QuizState state = new QuizState(1L, questionEntity.toDomainModel(), 0, Map.of("player1", 0), System.currentTimeMillis());
        quizStates.put(1L, state);
        
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = QuizService.class.getDeclaredField("quizStates");
            field.setAccessible(true);
            field.set(quizService, quizStates);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        // Act
        QuizState result = quizService.pauseQuiz(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.quizId());
        verify(quizRepository).save(quizEntity);
        assertEquals(QuizStatus.WAITING, quizEntity.getStatus());
    }

    @Test
    void endQuiz_Success() {
        // Arrange
        quizEntity.setStatus(QuizStatus.IN_PROGRESS);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quizEntity));
        when(quizPlayerRepository.findByQuiz(quizEntity))
                .thenReturn(Collections.singletonList(quizPlayerEntity));
        
        // Set up quiz state in the service
        Map<Long, QuizState> quizStates = new HashMap<>();
        QuizState state = new QuizState(1L, questionEntity.toDomainModel(), 0, Map.of("player1", 0), System.currentTimeMillis());
        quizStates.put(1L, state);
        
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = QuizService.class.getDeclaredField("quizStates");
            field.setAccessible(true);
            field.set(quizService, quizStates);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        // Act
        QuizState result = quizService.endQuiz(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.quizId());
        verify(quizRepository).save(quizEntity);
        assertEquals(QuizStatus.FINISHED, quizEntity.getStatus());
    }
}