package com.thonbecker.endurance.controller;

import com.thonbecker.endurance.service.QuizService;
import com.thonbecker.endurance.type.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/quiz/create")
    @SendTo("/topic/quiz/created")
    public Quiz createQuiz(Quiz quiz) {
        return quizService.createQuiz(quiz);
    }

    @MessageMapping("/quiz/create/trivia")
    @SendTo("/topic/quiz/created")
    public Quiz createTriviaQuiz(TriviaQuizRequest request) {
        return quizService.createQuizWithGeneratedQuestions(
                request.title(), request.questionCount(), request.difficulty());
    }

    @MessageMapping("/quiz/join")
    @SendTo("/topic/quiz/players")
    public List<Player> joinQuiz(JoinQuizRequest request) {
        return quizService.addPlayer(request.player(), request.quizId());
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

    @GetMapping("/api/quizzes")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/api/quizzes/{quizId}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long quizId) {
        return quizService
                .getQuizById(quizId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/quizzes/{quizId}/state")
    public ResponseEntity<QuizState> getQuizState(@PathVariable Long quizId) {
        QuizState state = quizService.getCurrentState(quizId);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }
}
