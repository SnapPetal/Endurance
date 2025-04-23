package com.thonbecker.endurance.service;

import com.thonbecker.endurance.type.Question;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TriviaQuestionGenerator {
    private static final String QUESTION_MARKER = "QUESTION:";
    private static final String OPTIONS_MARKER = "OPTIONS:";
    private static final String CORRECT_MARKER = "CORRECT:";
    private static final String EXPLANATION_MARKER = "EXPLANATION:";

    private static final Pattern OPTION_PATTERN = Pattern.compile("^([A-D]): (.+)$");
    private static final int DEFAULT_POINTS = 1; // Default points value for questions

    private final ChatClient chatClient;
    private final AtomicLong idCounter = new AtomicLong(1);

    public TriviaQuestionGenerator(ChatModel chatModel) {
        ChatOptions options = ChatOptions.builder()
                .model("anthropic.claude-3-sonnet-20240229-v1:0")
                .temperature(0.7)
                .maxTokens(2000)
                .topP(0.9)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("You are a financial knowledge expert specializing in Dave Ramsey's Baby Steps "
                        + "and personal finance. Create clear, accurate, and educational trivia questions.")
                .defaultOptions(options)
                .build();
    }

    /**
     * Generates Dave Ramsey financial trivia questions
     *
     * @param count The number of questions to generate
     * @param difficulty Level of difficulty (easy, medium, hard)
     * @return A list of generated Question objects
     */
    public List<Question> generateRamseyTrivia(int count, String difficulty) {
        String systemPromptText =
                """
            You are a financial trivia expert specializing in Dave Ramsey's teachings and principles.
            Create engaging multiple-choice trivia questions about Dave Ramsey's financial advice,
            baby steps, debt elimination strategies, investment recommendations, and other financial concepts he teaches.

            Each question should be %s difficulty and factually accurate to Dave Ramsey's actual teachings.

            Format your response as follows for each question:

            QUESTION: [The question text]
            OPTIONS:
            A: [Option A]
            B: [Option B]
            C: [Option C]
            D: [Option D]
            CORRECT: [The letter of the correct answer]
            EXPLANATION: [Brief explanation referencing the specific Dave Ramsey concept]
            """
                        .formatted(difficulty);

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptText);
        Message systemMessage = systemPromptTemplate.createMessage();

        String userPromptText = String.format("Generate %d Dave Ramsey financial trivia questions", count);
        Message userMessage = new UserMessage(userPromptText);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        String response = chatClient.prompt(prompt).call().content();

        return parseResponseToQuestions(response, calculatePointsForDifficulty(difficulty));
    }

    /**
     * Calculates points based on difficulty level
     *
     * @param difficulty The difficulty level
     * @return Points value for questions
     */
    private int calculatePointsForDifficulty(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 1;
            case "medium" -> 2;
            case "hard" -> 3;
            default -> DEFAULT_POINTS;
        };
    }

    /**
     * Parses the AI response into structured Question objects
     *
     * @param response The text response from the AI
     * @param points Points to assign to each question
     * @return A list of parsed Question objects
     */
    private List<Question> parseResponseToQuestions(String response, int points) {
        List<Question> questions = new ArrayList<>();
        String[] questionBlocks = response.split(QUESTION_MARKER);

        for (int i = 1; i < questionBlocks.length; i++) {
            String block = questionBlocks[i].trim();
            try {
                questions.add(parseQuestionBlock(block, points));
            } catch (Exception e) {
                log.error("Failed to parse question block: {}", e.getMessage());
            }
        }

        return questions;
    }

    /**
     * Parses a single question block into a Question object
     *
     * @param block The text block containing a single question
     * @param points Points to assign to the question
     * @return A Question object
     */
    private Question parseQuestionBlock(String block, int points) {
        // Extract question text
        String questionText = extractSection(block, 0, OPTIONS_MARKER);

        // Extract options
        String optionsSection = extractSection(block, OPTIONS_MARKER, CORRECT_MARKER);
        List<String> options = parseOptions(optionsSection);

        // Extract correct answer
        String correctAnswer = extractSection(block, CORRECT_MARKER, EXPLANATION_MARKER);
        int correctIndex = correctAnswer.trim().charAt(0) - 'A';

        // Extract explanation
        String explanation = "";
        if (block.contains(EXPLANATION_MARKER)) {
            explanation = block.substring(block.indexOf(EXPLANATION_MARKER) + EXPLANATION_MARKER.length())
                    .trim();
        }

        return new Question(generateQuestionId(), questionText, options, correctIndex, points);
    }

    /**
     * Extracts a section of text from the block
     *
     * @param block The text block
     * @param startMarker The starting marker (or 0 for beginning)
     * @param endMarker The ending marker
     * @return The extracted section
     */
    private String extractSection(String block, String startMarker, String endMarker) {
        int startIndex = block.indexOf(startMarker) + startMarker.length();
        int endIndex = block.indexOf(endMarker);
        return block.substring(startIndex, endIndex).trim();
    }

    private String extractSection(String block, int startIndex, String endMarker) {
        int endIndex = block.indexOf(endMarker);
        return block.substring(startIndex, endIndex).trim();
    }

    /**
     * Parses the options section into a list of option texts
     *
     * @param optionsSection The text containing all options
     * @return A list of options
     */
    private List<String> parseOptions(String optionsSection) {
        List<String> options = new ArrayList<>();
        String[] lines = optionsSection.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher matcher = OPTION_PATTERN.matcher(line);
            if (matcher.find()) {
                options.add(matcher.group(2));
            }
        }

        return options;
    }

    /**
     * Generates a unique ID for a question
     *
     * @return A unique ID
     */
    private Long generateQuestionId() {
        return idCounter.getAndIncrement();
    }
}
