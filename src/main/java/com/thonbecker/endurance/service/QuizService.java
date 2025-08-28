package com.thonbecker.endurance.service;

import com.thonbecker.endurance.entity.*;
import com.thonbecker.endurance.exception.InvalidStateException;
import com.thonbecker.endurance.exception.ResourceNotFoundException;
import com.thonbecker.endurance.exception.ValidationException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Failed to create quiz"));
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
    public List<Player> addPlayer(Player player, Long quizId) {
        // Check if the player exists
        PlayerEntity playerEntity = playerRepository.findById(player.id()).orElseGet(() -> {
            // Create a new player if not found
            PlayerEntity newPlayer = PlayerEntity.fromDomainModel(player);
            return playerRepository.save(newPlayer);
        });

        // Get the quiz
        QuizEntity quizEntity =
                quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

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
                quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        // Check if the quiz is in the correct state
        if (quizEntity.getStatus() != QuizStatus.CREATED && quizEntity.getStatus() != QuizStatus.WAITING) {
            throw new InvalidStateException(quizEntity.getStatus(), QuizStatus.CREATED, QuizStatus.WAITING);
        }

        // Update quiz status
        quizEntity.setStatus(QuizStatus.IN_PROGRESS);
        quizRepository.save(quizEntity);

        // Get the first question
        QuestionEntity firstQuestion = questionRepository.findByQuizOrderByQuestionOrderAsc(quizEntity).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No questions found for quiz"));

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
        // Validate submission
        if (submission == null) {
            throw new ValidationException("submission", "cannot be null");
        }
        if (submission.playerId() == null || submission.playerId().isEmpty()) {
            throw new ValidationException("playerId", "cannot be null or empty");
        }
        if (submission.quizId() == null) {
            throw new ValidationException("quizId", "cannot be null");
        }
        if (submission.questionId() == null) {
            throw new ValidationException("questionId", "cannot be null");
        }

        // Get entities
        QuizEntity quizEntity = quizRepository
                .findById(submission.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", submission.quizId()));

        PlayerEntity playerEntity = playerRepository
                .findById(submission.playerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", submission.playerId()));

        QuestionEntity questionEntity = questionRepository
                .findById(submission.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", submission.questionId()));

        // Check if the quiz is in progress
        if (quizEntity.getStatus() != QuizStatus.IN_PROGRESS) {
            throw new InvalidStateException(quizEntity.getStatus(), QuizStatus.IN_PROGRESS);
        }

        // Check if the question belongs to the quiz
        if (!questionEntity.getQuiz().getId().equals(quizEntity.getId())) {
            throw new ValidationException("Question does not belong to the specified quiz");
        }

        // Check if the player is part of the quiz
        if (!quizPlayerRepository.existsByQuizAndPlayer(quizEntity, playerEntity)) {
            throw new ValidationException("Player is not part of the quiz");
        }

        // Check if the player has already submitted an answer for this question
        if (answerSubmissionRepository.existsByQuizAndPlayerAndQuestion(quizEntity, playerEntity, questionEntity)) {
            throw new ValidationException("Player has already submitted an answer for this question");
        }

        // Validate selected option
        if (submission.selectedOption() < 0
                || submission.selectedOption() >= questionEntity.getOptions().size()) {
            throw new ValidationException("selectedOption", "invalid option index");
        }

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

        // Get the current quiz state
        QuizState currentState = quizStates.get(submission.quizId());
        if (currentState == null) {
            throw new ResourceNotFoundException("Quiz state for quiz", submission.quizId());
        }

        // Check if all players have answered the current question
        List<PlayerEntity> quizPlayers = quizPlayerRepository.findByQuiz(quizEntity).stream()
                .map(QuizPlayerEntity::getPlayer)
                .collect(Collectors.toList());

        List<String> playerIds = quizPlayers.stream().map(PlayerEntity::getId).collect(Collectors.toList());

        // Count answers for the current question
        long answerCount =
                answerSubmissionRepository.countByQuizAndQuestionAndPlayerIdIn(quizEntity, questionEntity, playerIds);

        // If all players have answered, move to the next question
        if (answerCount >= playerIds.size()) {
            return moveToNextQuestion(quizEntity, currentState);
        }

        // Otherwise, return the current state
        return currentState;
    }

    private QuizState moveToNextQuestion(QuizEntity quizEntity, QuizState currentState) {
        int nextQuestionIndex = currentState.currentQuestionIndex() + 1;

        // Get all questions for the quiz ordered by question order
        List<QuestionEntity> questions = questionRepository.findByQuizOrderByQuestionOrderAsc(quizEntity);

        // Check if there are more questions
        if (nextQuestionIndex < questions.size()) {
            // Get the next question
            QuestionEntity nextQuestion = questions.get(nextQuestionIndex);

            // Create player scores map
            Map<String, Integer> playerScores = quizPlayerRepository.findByQuiz(quizEntity).stream()
                    .collect(Collectors.toMap(qp -> qp.getPlayer().getId(), QuizPlayerEntity::getScore));

            // Create new quiz state with the next question
            QuizState newState = new QuizState(
                    quizEntity.getId(),
                    nextQuestion.toDomainModel(),
                    nextQuestionIndex,
                    playerScores,
                    System.currentTimeMillis());

            // Update in-memory state
            quizStates.put(quizEntity.getId(), newState);

            return newState;
        } else {
            // No more questions, quiz is finished
            quizEntity.setStatus(QuizStatus.FINISHED);
            quizRepository.save(quizEntity);

            // Get the final scores
            Map<String, Integer> finalScores = quizPlayerRepository.findByQuiz(quizEntity).stream()
                    .collect(Collectors.toMap(qp -> qp.getPlayer().getId(), QuizPlayerEntity::getScore));

            // Create final state (keeping the last question for reference)
            QuizState finalState = new QuizState(
                    quizEntity.getId(),
                    currentState.currentQuestion(),
                    currentState.currentQuestionIndex(),
                    finalScores,
                    currentState.questionStartTime());

            // Update in-memory state
            quizStates.put(quizEntity.getId(), finalState);

            return finalState;
        }
    }

    public QuizState getCurrentState(Long quizId) {
        return quizStates.get(quizId);
    }

    @Transactional
    public QuizState pauseQuiz(Long quizId) {
        // Validate input
        if (quizId == null) {
            throw new ValidationException("quizId", "cannot be null");
        }

        // Get the quiz
        QuizEntity quizEntity =
                quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        // Check if the quiz is in progress
        if (quizEntity.getStatus() != QuizStatus.IN_PROGRESS) {
            throw new InvalidStateException(quizEntity.getStatus(), QuizStatus.IN_PROGRESS);
        }

        // Update quiz status
        quizEntity.setStatus(QuizStatus.WAITING);
        quizRepository.save(quizEntity);

        // Get the current state
        QuizState currentState = quizStates.get(quizId);
        if (currentState == null) {
            throw new ResourceNotFoundException("Quiz state for quiz", quizId);
        }

        return currentState;
    }

    @Transactional
    public QuizState endQuiz(Long quizId) {
        // Validate input
        if (quizId == null) {
            throw new ValidationException("quizId", "cannot be null");
        }

        // Get the quiz
        QuizEntity quizEntity =
                quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        // Check if the quiz is in a state that can be ended
        if (quizEntity.getStatus() == QuizStatus.FINISHED) {
            throw new InvalidStateException("Quiz is already finished");
        }

        // Update quiz status
        quizEntity.setStatus(QuizStatus.FINISHED);
        quizRepository.save(quizEntity);

        // Get the current state
        QuizState currentState = quizStates.get(quizId);
        if (currentState == null) {
            throw new ResourceNotFoundException("Quiz state for quiz", quizId);
        }

        // Get the final scores
        Map<String, Integer> finalScores = quizPlayerRepository.findByQuiz(quizEntity).stream()
                .collect(Collectors.toMap(qp -> qp.getPlayer().getId(), QuizPlayerEntity::getScore));

        // Create final state
        QuizState finalState = new QuizState(
                quizEntity.getId(),
                currentState.currentQuestion(),
                currentState.currentQuestionIndex(),
                finalScores,
                currentState.questionStartTime());

        // Update in-memory state
        quizStates.put(quizId, finalState);

        return finalState;
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
