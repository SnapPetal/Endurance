package com.thonbecker.endurance.service;

import com.thonbecker.endurance.type.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class QuizService {
    private final Map<Long, Quiz> quizzes = new ConcurrentHashMap<>();
    private final Map<Long, QuizState> quizStates = new ConcurrentHashMap<>();
    private final Map<Long, List<Player>> quizPlayers = new ConcurrentHashMap<>();
    private final TriviaQuestionGenerator questionGenerator;

    public QuizService(TriviaQuestionGenerator questionGenerator) {
        this.questionGenerator = questionGenerator;
    }

    public Quiz createQuiz(Quiz quiz) {
        quizzes.put(quiz.id(), quiz);
        quizPlayers.put(quiz.id(), new ArrayList<>());
        return quiz;
    }

    public Quiz createQuizWithGeneratedQuestions(String title, int questionCount, String difficulty) {
        // Generate questions using the TriviaQuestionGenerator
        var questions = questionGenerator.generateRamseyTrivia(questionCount, difficulty);

        // Create a new Quiz with the generated questions
        var quiz = new Quiz(generateQuizId(), title, questions, 60, QuizStatus.CREATED);

        // Store the quiz
        return createQuiz(quiz);
    }

    private Long generateQuizId() {
        return System.currentTimeMillis();
    }

    public List<Player> addPlayer(Player player) {
        // Add player logic
        return new ArrayList<>(quizPlayers.get(1L));
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
        return quizStates.get(1L);
    }

    public QuizState getCurrentState(Long quizId) {
        return quizStates.get(quizId);
    }
}
