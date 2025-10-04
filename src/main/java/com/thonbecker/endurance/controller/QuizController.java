package com.thonbecker.endurance.controller;

import com.thonbecker.endurance.service.QuizService;
import com.thonbecker.endurance.type.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket endpoint to get available quizzes
    @MessageMapping("/quiz/list")
    @SendTo("/topic/quiz/list")
    public List<Quiz> getAvailableQuizzes() {
        return quizService.getAvailableQuizzes();
    }

    @MessageMapping("/quiz/create")
    @SendTo("/topic/quiz/created")
    public Quiz createQuiz(Quiz quiz) {
        Quiz createdQuiz = quizService.createQuiz(quiz);
        // Broadcast updated quiz list
        messagingTemplate.convertAndSend("/topic/quiz/list", quizService.getAvailableQuizzes());
        return createdQuiz;
    }

    @MessageMapping("/quiz/create/trivia")
    @SendTo("/topic/quiz/created")
    public Quiz createTriviaQuiz(TriviaQuizRequest request) {
        log.info("Received request to create a trivia quiz: {}", request);
        Quiz createdQuiz = quizService.createQuizWithGeneratedQuestions(
                request.title(), request.questionCount(), request.difficulty());
        // Broadcast updated quiz list
        messagingTemplate.convertAndSend("/topic/quiz/list", quizService.getAvailableQuizzes());
        return createdQuiz;
    }

    @MessageMapping("/quiz/join")
    @SendTo("/topic/quiz/players")
    public List<Player> joinQuiz(JoinQuizRequest request) {
        return quizService.addPlayer(request.player(), request.quizId());
    }

    @MessageMapping("/quiz/leave")
    @SendTo("/topic/quiz/players")
    public List<Player> leaveQuiz(LeaveQuizRequest request) {
        return quizService.removePlayer(request.playerId(), request.quizId());
    }

    @MessageMapping("/quiz/start")
    public void startQuiz(Long quizId) {
        QuizState state = quizService.startQuiz(quizId);
        messagingTemplate.convertAndSend("/topic/quiz/state/" + quizId, state);
    }

    @MessageMapping("/quiz/submit")
    public void submitAnswer(AnswerSubmission submission) {
        QuizState state = quizService.processAnswer(submission);
        messagingTemplate.convertAndSend("/topic/quiz/state/" + submission.quizId(), state);
    }

    @MessageMapping("/quiz/pause")
    public void pauseQuiz(Long quizId) {
        QuizState state = quizService.pauseQuiz(quizId);
        messagingTemplate.convertAndSend("/topic/quiz/state/" + quizId, state);
    }

    @MessageMapping("/quiz/end")
    public void endQuiz(Long quizId) {
        QuizState state = quizService.endQuiz(quizId);
        messagingTemplate.convertAndSend("/topic/quiz/state/" + quizId, state);
    }
}
