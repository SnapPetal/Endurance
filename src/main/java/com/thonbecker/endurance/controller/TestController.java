package com.thonbecker.endurance.controller;

import com.thonbecker.endurance.service.TriviaQuestionGenerator;
import com.thonbecker.endurance.type.Question;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller providing endpoints for testing and development purposes.
 * These endpoints are separate from the main WebSocket-based quiz functionality.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TriviaQuestionGenerator triviaQuestionGenerator;

    /**
     * Test endpoint for generating trivia questions without creating a quiz.
     * Useful for testing the question generation service in isolation.
     *
     * @param count The number of questions to generate (default: 5)
     * @param difficulty The difficulty level (easy, medium, hard) (default: medium)
     * @return A list of generated questions
     */
    @GetMapping("/generate-questions")
    public ResponseEntity<List<Question>> generateQuestions(
            @RequestParam(defaultValue = "5") int count, @RequestParam(defaultValue = "medium") String difficulty) {

        List<Question> questions = triviaQuestionGenerator.generateRamseyTrivia(count, difficulty);
        return ResponseEntity.ok(questions);
    }
}
