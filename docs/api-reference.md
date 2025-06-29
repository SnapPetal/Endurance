# Endurance API Reference

## Introduction

Endurance is a real-time quiz service that allows users to create, join, and participate in interactive quizzes. This document provides a comprehensive reference for the Endurance API, including WebSocket endpoints, message formats, and examples.

## Authentication and Security

- WebSocket connections are secured using STOMP headers
- Player sessions are managed via unique IDs
- Input validation is performed on all submissions

## REST API

Endurance provides a REST API for retrieving quiz information.

### Endpoints

#### Get All Quizzes

Retrieves a list of all quizzes.

- **Endpoint**: `/api/quizzes`
- **Method**: GET
- **Response**: List of Quiz objects

```json
[
  {
    "id": 1,
    "title": "My Custom Quiz",
    "questions": [
      {
        "id": 1,
        "questionText": "What is the capital of France?",
        "options": ["London", "Berlin", "Paris", "Madrid"],
        "correctOptionIndex": 2,
        "points": 10
      }
    ],
    "timePerQuestionInSeconds": 30,
    "status": "CREATED"
  }
]
```

#### Get Quiz by ID

Retrieves a specific quiz by its ID.

- **Endpoint**: `/api/quizzes/{quizId}`
- **Method**: GET
- **Response**: Quiz object

```json
{
  "id": 1,
  "title": "My Custom Quiz",
  "questions": [
    {
      "id": 1,
      "questionText": "What is the capital of France?",
      "options": ["London", "Berlin", "Paris", "Madrid"],
      "correctOptionIndex": 2,
      "points": 10
    }
  ],
  "timePerQuestionInSeconds": 30,
  "status": "CREATED"
}
```

#### Get Quiz State

Retrieves the current state of a quiz.

- **Endpoint**: `/api/quizzes/{quizId}/state`
- **Method**: GET
- **Response**: QuizState object

```json
{
  "quizId": 1,
  "currentQuestion": {
    "id": 1,
    "questionText": "What is the capital of France?",
    "options": ["London", "Berlin", "Paris", "Madrid"],
    "correctOptionIndex": 2,
    "points": 10
  },
  "currentQuestionIndex": 0,
  "playerScores": {
    "player123": 0,
    "player456": 0
  },
  "questionStartTime": 1623456789000
}
```

## WebSocket API

Endurance uses WebSocket for real-time communication. The service uses the STOMP protocol over SockJS for message handling.

### Connection

To connect to the Endurance WebSocket API:

```javascript
const socket = new SockJS('/websocket');
const stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

function onConnected() {
  console.log('Connected to Endurance WebSocket');
  // Subscribe to topics here
}

function onError(error) {
  console.error('Error connecting to Endurance WebSocket:', error);
}
```

### Endpoints

#### Create Quiz

Creates a new quiz with custom questions.

- **Endpoint**: `/app/quiz/create`
- **Method**: SEND
- **Request Body**: Quiz object

```json
{
  "id": null,
  "title": "My Custom Quiz",
  "questions": [
    {
      "id": null,
      "questionText": "What is the capital of France?",
      "options": ["London", "Berlin", "Paris", "Madrid"],
      "correctOptionIndex": 2,
      "points": 10
    }
  ],
  "timePerQuestionInSeconds": 30,
  "status": "CREATED"
}
```

- **Response Topic**: `/topic/quiz/created`
- **Response Body**: Created Quiz object with assigned IDs

```json
{
  "id": 1,
  "title": "My Custom Quiz",
  "questions": [
    {
      "id": 1,
      "questionText": "What is the capital of France?",
      "options": ["London", "Berlin", "Paris", "Madrid"],
      "correctOptionIndex": 2,
      "points": 10
    }
  ],
  "timePerQuestionInSeconds": 30,
  "status": "CREATED"
}
```

#### Create Trivia Quiz

Creates a new quiz with automatically generated trivia questions.

- **Endpoint**: `/app/quiz/create/trivia`
- **Method**: SEND
- **Request Body**: TriviaQuizRequest object

```json
{
  "title": "General Knowledge Trivia",
  "questionCount": 5,
  "difficulty": "medium"
}
```

- **Response Topic**: `/topic/quiz/created`
- **Response Body**: Created Quiz object with generated questions

```json
{
  "id": 2,
  "title": "General Knowledge Trivia",
  "questions": [
    {
      "id": 2,
      "questionText": "What is the largest planet in our solar system?",
      "options": ["Earth", "Mars", "Jupiter", "Saturn"],
      "correctOptionIndex": 2,
      "points": 10
    }
  ],
  "timePerQuestionInSeconds": 30,
  "status": "CREATED"
}
```

#### Join Quiz

Allows a player to join a quiz.

- **Endpoint**: `/app/quiz/join`
- **Method**: SEND
- **Request Body**: Player object

```json
{
  "id": "player123",
  "name": "John Doe",
  "score": 0,
  "isReady": true
}
```

- **Response Topic**: `/topic/quiz/players`
- **Response Body**: List of all players in the quiz

```json
[
  {
    "id": "player123",
    "name": "John Doe",
    "score": 0,
    "isReady": true
  },
  {
    "id": "player456",
    "name": "Jane Smith",
    "score": 0,
    "isReady": false
  }
]
```

#### Start Quiz

Starts a quiz.

- **Endpoint**: `/app/quiz/start`
- **Method**: SEND
- **Request Body**: Quiz ID (Long)

```json
1
```

- **Response Topic**: `/topic/quiz/state/{quizId}`
- **Response Body**: QuizState object

```json
{
  "quizId": 1,
  "currentQuestion": {
    "id": 1,
    "questionText": "What is the capital of France?",
    "options": ["London", "Berlin", "Paris", "Madrid"],
    "correctOptionIndex": 2,
    "points": 10
  },
  "currentQuestionIndex": 0,
  "playerScores": {
    "player123": 0,
    "player456": 0
  },
  "questionStartTime": 1623456789000
}
```

#### Submit Answer

Submits an answer to a quiz question.

- **Endpoint**: `/app/quiz/submit`
- **Method**: SEND
- **Request Body**: AnswerSubmission object

```json
{
  "playerId": "player123",
  "quizId": 1,
  "questionId": 1,
  "selectedOption": 2,
  "submissionTime": 1623456799000
}
```

- **Response Topic**: `/topic/quiz/state/{quizId}`
- **Response Body**: Updated QuizState object

```json
{
  "quizId": 1,
  "currentQuestion": {
    "id": 2,
    "questionText": "What is the largest mammal?",
    "options": ["Elephant", "Blue Whale", "Giraffe", "Hippopotamus"],
    "correctOptionIndex": 1,
    "points": 10
  },
  "currentQuestionIndex": 1,
  "playerScores": {
    "player123": 10,
    "player456": 0
  },
  "questionStartTime": 1623456800000
}
```

#### Pause Quiz

Pauses a quiz that is in progress.

- **Endpoint**: `/app/quiz/pause`
- **Method**: SEND
- **Request Body**: Quiz ID (Long)

```json
1
```

- **Response Topic**: `/topic/quiz/state/{quizId}`
- **Response Body**: QuizState object

```json
{
  "quizId": 1,
  "currentQuestion": {
    "id": 1,
    "questionText": "What is the capital of France?",
    "options": ["London", "Berlin", "Paris", "Madrid"],
    "correctOptionIndex": 2,
    "points": 10
  },
  "currentQuestionIndex": 0,
  "playerScores": {
    "player123": 0,
    "player456": 0
  },
  "questionStartTime": 1623456789000
}
```

#### End Quiz

Ends a quiz, regardless of its current state.

- **Endpoint**: `/app/quiz/end`
- **Method**: SEND
- **Request Body**: Quiz ID (Long)

```json
1
```

- **Response Topic**: `/topic/quiz/state/{quizId}`
- **Response Body**: Final QuizState object

```json
{
  "quizId": 1,
  "currentQuestion": {
    "id": 1,
    "questionText": "What is the capital of France?",
    "options": ["London", "Berlin", "Paris", "Madrid"],
    "correctOptionIndex": 2,
    "points": 10
  },
  "currentQuestionIndex": 0,
  "playerScores": {
    "player123": 10,
    "player456": 5
  },
  "questionStartTime": 1623456789000
}
```

## Data Models

### Quiz

```
{
  "id": 1,                              // Long: Unique identifier for the quiz
  "title": "My Quiz",                   // String: Title of the quiz
  "questions": [                        // Array of Question objects
    {
      "id": 1,
      "questionText": "Sample question",
      "options": ["Option 1", "Option 2"],
      "correctOptionIndex": 0,
      "points": 10
    }
  ],
  "timePerQuestionInSeconds": 30,       // int: Time allowed per question
  "status": "IN_PROGRESS"               // QuizStatus: Current status of the quiz
}
```

### Question

```
{
  "id": 1,                              // Long: Unique identifier for the question
  "questionText": "Sample question",    // String: The text of the question
  "options": ["Option 1", "Option 2"],  // Array of String: Possible answers
  "correctOptionIndex": 0,              // int: Index of the correct answer
  "points": 10                          // int: Points awarded for correct answer
}
```

### Player

```
{
  "id": "player123",                    // String: Unique identifier for the player
  "name": "John Doe",                   // String: Player's name
  "score": 0,                           // int: Player's current score
  "isReady": true                       // boolean: Whether the player is ready
}
```

### AnswerSubmission

```
{
  "playerId": "player123",              // String: ID of the player submitting the answer
  "quizId": 1,                          // Long: ID of the quiz
  "questionId": 1,                      // Long: ID of the question being answered
  "selectedOption": 0,                  // int: Index of the selected answer option
  "submissionTime": 1623456789000       // long: Timestamp of submission
}
```

### QuizState

```
{
  "quizId": 1,                          // Long: ID of the quiz
  "currentQuestion": {                  // Question: Current question being asked
    "id": 1,
    "questionText": "Sample question",
    "options": ["Option 1", "Option 2"],
    "correctOptionIndex": 0,
    "points": 10
  },
  "currentQuestionIndex": 0,            // int: Index of the current question
  "playerScores": {                     // Map of player IDs to scores
    "player123": 10,
    "player456": 5
  },
  "questionStartTime": 1623456789000    // long: Timestamp when question started
}
```

### QuizStatus

Enum with the following values:
- `WAITING` - The quiz is waiting for players to join
- `IN_PROGRESS` - The quiz is currently in progress
- `CREATED` - The quiz has been created but not yet started
- `FINISHED` - The quiz has finished

### TriviaQuizRequest

```
{
  "title": "Trivia Quiz",               // String: Title of the quiz
  "questionCount": 5,                   // int: Number of questions to generate
  "difficulty": "medium"                // String: Difficulty level of questions
}
```

## Error Handling

Errors are communicated through the WebSocket connection. Common errors include:

- Invalid quiz ID
- Invalid question ID
- Invalid player ID
- Invalid answer submission (e.g., out of time, invalid option index)
- Quiz not in the correct state for the requested operation

When an error occurs, the server will not update the quiz state and may send an error message to the client.

## Rate Limiting

To prevent abuse, the API implements rate limiting. Clients should implement exponential backoff when retrying failed requests.

## Best Practices

1. Always check the quiz status before performing operations
2. Implement proper error handling for WebSocket disconnections
3. Synchronize client-side timers with the server's `questionStartTime`
4. Validate user input before sending to the server
5. Implement reconnection logic for WebSocket connections

## Web Client Implementation Guide

This section provides a step-by-step guide for implementing a web client that connects to the Endurance quiz service.

### Setting Up Your Web Project

1. Create a basic HTML structure:
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Endurance Quiz Client</title>
    <!-- Include SockJS and STOMP libraries -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div id="app">
        <h1>Endurance Quiz</h1>
        <div id="quiz-container">
            <!-- Quiz UI will be dynamically generated here -->
        </div>
    </div>
    <script src="app.js"></script>
</body>
</html>
```

2. Create an `app.js` file with the WebSocket connection logic:
```javascript
// Global variables
let stompClient = null;
let currentQuizId = null;
let playerId = generateUniqueId();
let playerName = "Player1";

// Connect to WebSocket
function connect() {
    const socket = new SockJS('http://localhost:8080/quiz-websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

// On successful connection
function onConnected() {
    console.log('Connected to Endurance WebSocket');

    // If we have a quiz ID, subscribe to its topics
    if (currentQuizId) {
        subscribeToQuizTopics(currentQuizId);
    }

    // Show the UI for creating or joining a quiz
    showInitialUI();
}

// On connection error
function onError(error) {
    console.error('WebSocket connection error:', error);

    // Implement reconnection with exponential backoff
    setTimeout(() => {
        console.log('Attempting to reconnect...');
        connect();
    }, 5000);
}

// Subscribe to topics for a specific quiz
function subscribeToQuizTopics(quizId) {
    currentQuizId = quizId;

    // Subscribe to quiz state updates
    stompClient.subscribe('/topic/quiz/state/' + quizId, function(response) {
        const quizState = JSON.parse(response.body);
        handleQuizStateUpdate(quizState);
    });

    // Subscribe to player list updates
    stompClient.subscribe('/topic/quiz/players/' + quizId, function(response) {
        const players = JSON.parse(response.body);
        updatePlayerList(players);
    });
}

// Generate a unique ID for the player
function generateUniqueId() {
    return 'player-' + Math.random().toString(36).substr(2, 9);
}

// Initialize the application
function init() {
    connect();
}

// Call init when the page loads
window.onload = init;
```

### Creating a Quiz

Add the following functions to your JavaScript file:

```javascript
// Create a new quiz
function createQuiz() {
    const quizTitle = document.getElementById('quiz-title').value;
    const timePerQuestion = parseInt(document.getElementById('time-per-question').value);

    const quiz = {
        title: quizTitle,
        questions: [
            {
                questionText: "What is the capital of France?",
                options: ["London", "Paris", "Berlin", "Madrid"],
                correctOptionIndex: 1,
                points: 10
            },
            {
                questionText: "What is 2 + 2?",
                options: ["3", "4", "5", "6"],
                correctOptionIndex: 1,
                points: 5
            }
        ],
        timePerQuestionInSeconds: timePerQuestion,
        status: "CREATED"
    };

    // Send the quiz creation message
    stompClient.send("/app/quiz/create", {}, JSON.stringify(quiz));

    // Subscribe to the created quiz topic
    stompClient.subscribe('/topic/quiz/created', function(response) {
        const createdQuiz = JSON.parse(response.body);
        currentQuizId = createdQuiz.id;

        // Subscribe to the quiz topics
        subscribeToQuizTopics(currentQuizId);

        // Show the quiz host UI
        showQuizHostUI(createdQuiz);
    });
}
```

### Joining a Quiz

```javascript
// Join an existing quiz
function joinQuiz(quizId) {
    currentQuizId = quizId;

    // Subscribe to the quiz topics
    subscribeToQuizTopics(quizId);

    // Create player object
    const player = {
        id: playerId,
        name: playerName,
        score: 0,
        isReady: true
    };

    // Send the join message
    stompClient.send("/app/quiz/join", {}, JSON.stringify(player));

    // Show the player UI
    showPlayerUI();
}
```

### Submitting Answers

```javascript
// Submit an answer to a question
function submitAnswer(questionId, selectedOptionIndex) {
    const answer = {
        playerId: playerId,
        quizId: currentQuizId,
        questionId: questionId,
        selectedOption: selectedOptionIndex,
        submissionTime: Date.now()
    };

    stompClient.send("/app/quiz/submit", {}, JSON.stringify(answer));
}
```

### Handling Quiz State Updates

```javascript
// Handle quiz state updates
function handleQuizStateUpdate(quizState) {
    console.log('Quiz state updated:', quizState);

    // Update the UI based on the quiz state
    if (quizState.currentQuestion) {
        showQuestion(quizState.currentQuestion);
    }

    // Update scores
    updateScores(quizState.playerScores);

    // If the quiz is finished, show results
    if (quizState.status === "FINISHED") {
        showResults(quizState);
    }
}
```

### Complete Example

For a complete working example of a web client implementation, you can refer to the sample project in the repository's `examples/web-client` directory.

The example includes:
- A complete HTML/CSS/JavaScript implementation
- Quiz creation and joining functionality
- Real-time question display and answer submission
- Score tracking and results display
- Error handling and reconnection logic
