package com.thonbecker.endurance.controller;

import com.thonbecker.endurance.service.QuizService;
import com.thonbecker.endurance.type.AnswerSubmission;
import com.thonbecker.endurance.type.Player;
import com.thonbecker.endurance.type.Quiz;
import com.thonbecker.endurance.type.QuizState;
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

    @MessageMapping("/quiz/join")
    @SendTo("/topic/quiz/players")
    public List<Player> joinQuiz(Player player) {
        return quizService.addPlayer(player);
    }

    @MessageMapping("/quiz/start")
    public void startQuiz(Long quizId) {
        QuizState state = quizService.startQuiz(quizId);
        messagingTemplate.convertAndSend("/topic/quiz/state", state);
        messagingTemplate.convertAndSend("/topic/quiz/question", state.currentQuestion());
    }

    @MessageMapping("/quiz/submit")
    public void submitAnswer(AnswerSubmission submission) {
        QuizState state = quizService.processAnswer(submission);
        messagingTemplate.convertAndSend("/topic/quiz/state", state);
    }
}
