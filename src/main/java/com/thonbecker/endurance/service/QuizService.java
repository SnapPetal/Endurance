package com.thonbecker.endurance.service;

import com.thonbecker.endurance.entity.*;
import com.thonbecker.endurance.repository.*;
import com.thonbecker.endurance.type.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
    // In-memory state for active quizzes
    private final Map<Long, QuizState> quizStates = new ConcurrentHashMap<>();

    // Services and repositories
    private final TriviaQuestionGenerator questionGenerator;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final PlayerRepository playerRepository;
    private final QuizPlayerRepository quizPlayerRepository;
    private final AnswerSubmissionRepository answerSubmissionRepository;

    public QuizService(
            TriviaQuestionGenerator questionGenerator,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            PlayerRepository playerRepository,
            QuizPlayerRepository quizPlayerRepository,
            AnswerSubmissionRepository answerSubmissionRepository) {
        this.questionGenerator = questionGenerator;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.playerRepository = playerRepository;
        this.quizPlayerRepository = quizPlayerRepository;
        this.answerSubmissionRepository = answerSubmissionRepository;
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        // Convert domain model to entity
        QuizEntity quizEntity = QuizEntity.fromDomainModel(quiz);

        // Save the quiz entity
        quizEntity = quizRepository.save(quizEntity);

        // Save questions and options
        for (int i = 0; i < quiz.questions().size(); i++) {
            Question question = quiz.questions().get(i);
            QuestionEntity questionEntity = QuestionEntity.fromDomainModel(question, i);
            questionEntity.setQuiz(quizEntity);
            questionEntity = questionRepository.save(questionEntity);

            // Save options
            List<String> options = question.options();
            for (int j = 0; j < options.size(); j++) {
                QuestionOptionEntity optionEntity = new QuestionOptionEntity(options.get(j), j);
                optionEntity.setQuestion(questionEntity);
                questionOptionRepository.save(optionEntity);
            }
        }

        // Return the saved quiz as domain model
        return quizRepository
                .findById(quizEntity.getId())
                .map(QuizEntity::toDomainModel)
                .orElseThrow(() -> new RuntimeException("Failed to create quiz"));
    }

    @Transactional
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

    @Transactional
    public List<Player> addPlayer(Player player) {
        // Check if the player exists
        PlayerEntity playerEntity = playerRepository.findById(player.id()).orElseGet(() -> {
            // Create a new player if not found
            PlayerEntity newPlayer = PlayerEntity.fromDomainModel(player);
            return playerRepository.save(newPlayer);
        });

        // Get the quiz
        QuizEntity quizEntity = quizRepository.findById(1L).orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Check if the player is already in the quiz
        if (!quizPlayerRepository.existsByQuizAndPlayer(quizEntity, playerEntity)) {
            // Add the player to the quiz
            QuizPlayerEntity quizPlayerEntity = new QuizPlayerEntity(quizEntity, playerEntity);
            quizPlayerEntity.setReady(player.isReady());
            quizPlayerRepository.save(quizPlayerEntity);
        } else {
            // Update player readiness
            quizPlayerRepository.findByQuizAndPlayer(quizEntity, playerEntity).ifPresent(qp -> {
                qp.setReady(player.isReady());
                quizPlayerRepository.save(qp);
            });
        }

        // Return all players in the quiz
        return quizPlayerRepository.findByQuiz(quizEntity).stream()
                .map(qp -> qp.getPlayer().toDomainModel(quizEntity.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizState startQuiz(Long quizId) {
        // Get the quiz
        QuizEntity quizEntity =
                quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Update quiz status
        quizEntity.setStatus(QuizStatus.IN_PROGRESS);
        quizRepository.save(quizEntity);

        // Get the first question
        QuestionEntity firstQuestion = questionRepository.findByQuizOrderByQuestionOrderAsc(quizEntity).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No questions found for quiz"));

        // Create player scores map
        Map<String, Integer> playerScores = quizPlayerRepository.findByQuiz(quizEntity).stream()
                .collect(Collectors.toMap(qp -> qp.getPlayer().getId(), QuizPlayerEntity::getScore));

        // Create quiz state
        QuizState state =
                new QuizState(quizId, firstQuestion.toDomainModel(), 0, playerScores, System.currentTimeMillis());

        // Store in memory for active state
        quizStates.put(quizId, state);

        return state;
    }

    @Transactional
    public QuizState processAnswer(AnswerSubmission submission) {
        // Get entities
        QuizEntity quizEntity =
                quizRepository.findById(submission.quizId()).orElseThrow(() -> new RuntimeException("Quiz not found"));

        PlayerEntity playerEntity = playerRepository
                .findById(submission.playerId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        QuestionEntity questionEntity = questionRepository
                .findById(submission.questionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Save the answer submission
        AnswerSubmissionEntity answerEntity =
                AnswerSubmissionEntity.fromDomainModel(submission, playerEntity, quizEntity, questionEntity);
        answerSubmissionRepository.save(answerEntity);

        // Check if the answer is correct
        boolean isCorrect = questionEntity.getCorrectOptionIndex() == submission.selectedOption();

        // Update player score if correct
        if (isCorrect) {
            quizPlayerRepository.findByQuizAndPlayer(quizEntity, playerEntity).ifPresent(qp -> {
                qp.setScore(qp.getScore() + questionEntity.getPoints());
                quizPlayerRepository.save(qp);
            });
        }

        // For now, just return the current state
        // In a real implementation, you would check if all players have answered
        // and move to the next question if needed
        return getCurrentState(submission.quizId());
    }

    public QuizState getCurrentState(Long quizId) {
        return quizStates.get(quizId);
    }

    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll().stream().map(QuizEntity::toDomainModel).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Quiz> getQuizById(Long quizId) {
        return quizRepository.findById(quizId).map(QuizEntity::toDomainModel);
    }
}
