
## Models

### Quiz Record
```java
public record Quiz(
    Long id,
    String title,
    List<Question> questions,
    int timePerQuestionInSeconds,
    QuizStatus status
){}
```

### Question Record
```java
public record Question(
    Long id,
    String questionText,
    List<String> options,
    int correctOptionIndex,
    int points
){}
```

### Player Record
```java
public record Player(
    String id,
    String name,
    int score,
    boolean isReady
){}
```

## WebSocket Endpoints

### Message Mappings
- `/quiz/join` - Player joins quiz
- `/quiz/ready` - Player indicates ready status
- `/quiz/submit` - Submit answer
- `/quiz/start` - Admin starts quiz
- `/quiz/next` - Move to next question

### Subscription Topics
- `/topic/quiz/state` - Quiz state updates
- `/topic/quiz/players` - Player list updates
- `/topic/quiz/question` - Current question
- `/topic/quiz/results` - Question results

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Modern web browser with WebSocket support

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/quiz-system.git
```

2. Navigate to project directory:
```bash
cd quiz-system
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

### Configuration

Application properties can be configured in `src/main/resources/application.properties`:

```properties
server.port=8080
spring.application.name=quiz-system
```

## Usage

### Creating a Quiz

1. Access the admin panel
2. Create a new quiz with questions
3. Set time limit per question
4. Save the quiz

### Joining a Quiz

1. Open the quiz URL
2. Enter your name
3. Wait for the host to start

### Hosting a Quiz

1. Login as admin
2. Select a quiz to host
3. Wait for players to join
4. Start the quiz

### Playing a Quiz

1. Join using your name
2. Wait for questions to appear
3. Select answers within the time limit
4. View results after each question

## API Documentation

### Quiz Creation
```http
POST /api/quiz
Content-Type: application/json

{
  "title": "Geography Quiz",
  "questions": [
    {
      "questionText": "What is the capital of France?",
      "options": ["London", "Paris", "Berlin", "Madrid"],
      "correctOptionIndex": 1,
      "points": 5
    }
  ],
  "timePerQuestionInSeconds": 30
}
```

### Player Join
```http
POST /api/quiz/{quizId}/join
Content-Type: application/json

{
  "name": "John Doe"
}
```

## WebSocket Messages

### Join Quiz
```javascript
stompClient.send("/app/quiz/join", {}, JSON.stringify({
    id: "playerId",
    name: "Player Name",
    score: 0,
    isReady: true
}));
```

### Submit Answer
```javascript
stompClient.send("/app/quiz/submit", {}, JSON.stringify({
    playerId: "playerId",
    questionId: 1,
    selectedOption: 2,
    submissionTime: timestamp
}));
```

## Security

- WebSocket connections are secured using STOMP headers
- Player sessions are managed via unique IDs
- Input validation on all submissions

## Development

### Building from Source

```bash
git clone https://github.com/yourusername/quiz-system.git
cd quiz-system
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Code Style

This project follows the Google Java Style Guide. Please ensure your code is formatted accordingly before submitting PRs.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Framework team for the WebSocket implementation
- SockJS team for the client-side library
- All contributors who participate in this project

## Contact

For any queries, please open an issue or contact the maintainers.