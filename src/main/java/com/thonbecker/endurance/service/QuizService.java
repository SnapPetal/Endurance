package com.thonbecker.endurance.service;

import com.thonbecker.endurance.type.AnswerSubmission;
import com.thonbecker.endurance.type.Player;
import com.thonbecker.endurance.type.Quiz;
import com.thonbecker.endurance.type.QuizState;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class QuizService {
    private final Map<Long, Quiz> quizzes = new ConcurrentHashMap<>();
    private final Map<Long, QuizState> quizStates = new ConcurrentHashMap<>();
    private final Map<Long, List<Player>> quizPlayers = new ConcurrentHashMap<>();

    public Quiz createQuiz(Quiz quiz) {
        quizzes.put(quiz.id(), quiz);
        quizPlayers.put(quiz.id(), new ArrayList<>());
        return quiz;
    }

    public List<Player> addPlayer(Player player) {
        // Add player logic
        return new ArrayList<>(quizPlayers.get(1L)); // Simplified for example
    }

    public QuizState startQuiz(Long quizId) {
        Quiz quiz = quizzes.get(quizId);
        QuizState state =
                new QuizState(quizId, quiz.questions().getFirst(), 0, new HashMap<>(), System.currentTimeMillis());
        quizStates.put(quizId, state);
        return state;
    }

    public QuizState processAnswer(AnswerSubmission submission) {
        // Process answer logic
        return quizStates.get(1L); // Simplified for example
    }

    public QuizState getCurrentState(Long quizId) {
        return quizStates.get(quizId);
    }
}
