package com.thonbecker.endurance.controller;

import com.thonbecker.endurance.service.QuizService;
import com.thonbecker.endurance.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizControllerTest {

    @Mock
    private QuizService quizService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private QuizController quizController;

    private Quiz quiz;
    private QuizState quizState;
    private Player player;
    private AnswerSubmission answerSubmission;

    @BeforeEach
    void setUp() {
        // Set up test data
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        Question question = new Question(1L, "Test Question", options, 0, 10);
        List<Question> questions = Collections.singletonList(question);
        quiz = new Quiz(1L, "Test Quiz", questions, 30, QuizStatus.CREATED);
        
        Map<String, Integer> playerScores = new HashMap<>();
        playerScores.put("player1", 0);
        quizState = new QuizState(1L, question, 0, playerScores, System.currentTimeMillis());
        
        player = new Player("player1", "Test Player", 0, true);
        
        answerSubmission = new AnswerSubmission("player1", 1L, 1L, 0, System.currentTimeMillis());
    }

    @Test
    void createQuiz_Success() {
        // Arrange
        when(quizService.createQuiz(any(Quiz.class))).thenReturn(quiz);
        
        // Act
        Quiz result = quizController.createQuiz(quiz);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Quiz", result.title());
    }

    @Test
    void getAllQuizzes_Success() {
        // Arrange
        when(quizService.getAllQuizzes()).thenReturn(Collections.singletonList(quiz));
        
        // Act
        ResponseEntity<List<Quiz>> response = quizController.getAllQuizzes();
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).id());
    }

    @Test
    void getQuizById_Success() {
        // Arrange
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        
        // Act
        ResponseEntity<Quiz> response = quizController.getQuizById(1L);
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    void getQuizById_NotFound() {
        // Arrange
        when(quizService.getQuizById(1L)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<Quiz> response = quizController.getQuizById(1L);
        
        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void getQuizState_Success() {
        // Arrange
        when(quizService.getCurrentState(1L)).thenReturn(quizState);
        
        // Act
        ResponseEntity<QuizState> response = quizController.getQuizState(1L);
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(1L, response.getBody().quizId());
    }

    @Test
    void getQuizState_NotFound() {
        // Arrange
        when(quizService.getCurrentState(1L)).thenReturn(null);
        
        // Act
        ResponseEntity<QuizState> response = quizController.getQuizState(1L);
        
        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void startQuiz_Success() {
        // Arrange
        when(quizService.startQuiz(1L)).thenReturn(quizState);
        
        // Act
        quizController.startQuiz(1L);
        
        // Assert
        verify(quizService).startQuiz(1L);
        verify(messagingTemplate).convertAndSend("/topic/quiz/state/1", quizState);
    }

    @Test
    void pauseQuiz_Success() {
        // Arrange
        when(quizService.pauseQuiz(1L)).thenReturn(quizState);
        
        // Act
        quizController.pauseQuiz(1L);
        
        // Assert
        verify(quizService).pauseQuiz(1L);
        verify(messagingTemplate).convertAndSend("/topic/quiz/state/1", quizState);
    }

    @Test
    void endQuiz_Success() {
        // Arrange
        when(quizService.endQuiz(1L)).thenReturn(quizState);
        
        // Act
        quizController.endQuiz(1L);
        
        // Assert
        verify(quizService).endQuiz(1L);
        verify(messagingTemplate).convertAndSend("/topic/quiz/state/1", quizState);
    }

    @Test
    void submitAnswer_Success() {
        // Arrange
        when(quizService.processAnswer(answerSubmission)).thenReturn(quizState);
        
        // Act
        quizController.submitAnswer(answerSubmission);
        
        // Assert
        verify(quizService).processAnswer(answerSubmission);
        verify(messagingTemplate).convertAndSend("/topic/quiz/state/" + answerSubmission.quizId(), quizState);
    }
}