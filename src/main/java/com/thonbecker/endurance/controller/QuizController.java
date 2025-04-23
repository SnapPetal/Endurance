package com.thonbecker.endurance.controller;

import com.thonbecker.endurance.service.QuizService;
import com.thonbecker.endurance.type.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
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
    public List<Player> joinQuiz(Player player) {
        return quizService.addPlayer(player);
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
}
