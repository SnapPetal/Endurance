# Endurance Quiz Service

Endurance is a real-time quiz service that allows client applications to create, host, and participate in interactive quizzes. The service uses **WebSocket-only communication** for all operations, enabling real-time features like live question updates, answer submissions, and score tracking without the need for REST APIs.

## Documentation

For a comprehensive understanding of the Endurance service, please refer to the following documentation:

- [Project Requirements](docs/requirements.md) - Detailed functional and technical requirements
- [Improvement Plan](docs/plan.md) - Comprehensive plan for enhancing the service

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

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL database
- AWS account for Bedrock AI services (optional, for question generation)

### Deploying the Service

1. Clone the repository:
```bash
git clone https://github.com/SnapPetal/Endurance.git
```

2. Navigate to project directory:
```bash
cd endurance
```

3. Configure environment variables:

Copy the `.env.example` file to `.env` and fill in the required values.

```bash
cp .env.example .env
```

4. Build the project:
```bash
mvn clean install
```

5. Run the service:
```bash
mvn spring-boot:run
```

The service will be available at `http://localhost:8080`

### Configuration

The service can be configured through environment variables or by modifying the application configuration files:

#### application.yml
```yaml
spring:
  application:
    name: endurance
  datasource:
    url: jdbc:postgresql://your-database-host:5432/dbname
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

server:
  port: 8080
```

#### Development Configuration
For local development, use the `application-dev.yml` profile:

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Database Schema

The application uses a PostgreSQL database with the following schema:

#### Tables
- **quiz** - Stores quiz information (title, time per question, status)
- **question** - Stores question information (text, correct option index, points)
- **question_option** - Stores the options for each question
- **player** - Stores player information (name)
- **quiz_player** - Join table for quiz-player relationships (scores, readiness)
- **answer_submission** - Stores player answer submissions

#### Entity Relationships
- A Quiz has many Questions
- A Question belongs to one Quiz and has many Options
- A Player can participate in many Quizzes (via quiz_player)
- A Quiz can have many Players (via quiz_player)
- Answer Submissions are linked to a Quiz, Question, and Player

The database schema is managed using Liquibase migrations, which can be found in the `src/main/resources/db/changelog/changes/` directory.

## Service Integration

### Implementing the Client

To integrate the Endurance quiz service into your application:

1. Set up WebSocket connection to the service
2. Implement message handlers for quiz state updates
3. Create user interface components for quiz interaction
4. Handle quiz lifecycle events (join, ready, submit, etc.)

### Using the API

The Endurance service provides a WebSocket-only API for real-time quiz interaction:

1. Use WebSocket messages for all quiz operations (create, join, start, submit, etc.)
2. Subscribe to relevant topics for state updates
3. Send messages to appropriate destinations for actions
4. All communication is real-time via WebSocket

### Running from a Web Client

To run the Endurance quiz service from a web client:

1. **Prerequisites**:
   - Modern web browser with WebSocket support
   - JavaScript environment with SockJS and STOMP libraries

2. **Setup**:
   - Include the required libraries in your HTML:
   ```html
   <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
   <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
   ```

3. **Connect to the WebSocket**:
   ```javascript
   // Connect to the WebSocket endpoint
   const socket = new SockJS('http://localhost:8080/quiz-websocket');
   const stompClient = Stomp.over(socket);

   stompClient.connect({}, function(frame) {
     console.log('Connected: ' + frame);

     // Subscribe to topics for a specific quiz
     const quizId = 1; // Replace with your quiz ID

     // Subscribe to quiz state updates
     stompClient.subscribe('/topic/quiz/state/' + quizId, function(response) {
       const quizState = JSON.parse(response.body);
       console.log('Quiz state updated:', quizState);
       // Update your UI based on the quiz state
     });

     // Subscribe to player list updates
     stompClient.subscribe('/topic/quiz/players/' + quizId, function(response) {
       const players = JSON.parse(response.body);
       console.log('Player list updated:', players);
       // Update your player list UI
     });
   }, function(error) {
     console.error('Connection error:', error);
   });
   ```

4. **Join a Quiz**:
   ```javascript
   // Join a quiz as a player
   const player = {
     id: generateUniqueId(), // Implement a function to generate a unique ID
     name: 'Player Name',
     score: 0,
     isReady: true
   };

   stompClient.send('/app/quiz/join', {}, JSON.stringify(player));
   ```

5. **Submit an Answer**:
   ```javascript
   // Submit an answer to a question
   const answer = {
     playerId: player.id,
     quizId: quizId,
     questionId: currentQuestion.id,
     selectedOption: selectedOptionIndex,
     submissionTime: Date.now()
   };

   stompClient.send('/app/quiz/submit', {}, JSON.stringify(answer));
   ```

6. **Create a Quiz** (using WebSocket):
   ```javascript
   // Create a new quiz via WebSocket
   const quizData = {
     title: 'My Quiz',
     questions: [
       {
         questionText: 'What is the capital of France?',
         options: ['London', 'Paris', 'Berlin', 'Madrid'],
         correctOptionIndex: 1,
         points: 10
       }
     ],
     timePerQuestionInSeconds: 30
   };

   stompClient.send('/app/quiz/create', {}, JSON.stringify(quizData));
   
   // Subscribe to quiz creation confirmation
   stompClient.subscribe('/topic/quiz/created', function(response) {
     const quiz = JSON.parse(response.body);
     console.log('Created quiz:', quiz);
     // Use the created quiz ID for other operations
   });
   ```

7. **Error Handling**:
   ```javascript
   // Implement reconnection logic
   function connectWebSocket() {
     const socket = new SockJS('http://localhost:8080/quiz-websocket');
     const stompClient = Stomp.over(socket);

     stompClient.connect({}, onConnected, onError);

     return stompClient;
   }

   function onError(error) {
     console.error('WebSocket connection error:', error);
     console.log('Attempting to reconnect in 5 seconds...');
     setTimeout(connectWebSocket, 5000);
   }
   ```

8. **Disconnecting**:
   ```javascript
   // Disconnect when done
   function disconnect() {
     if (stompClient !== null) {
       stompClient.disconnect();
       console.log('Disconnected from WebSocket');
     }
   }
   ```

For a complete example of a web client implementation, refer to the [API Reference Documentation](docs/api-reference.md).

### Quiz Lifecycle

A typical quiz integration flow:

1. Create a quiz using WebSocket messages (`/app/quiz/create` or `/app/quiz/create/trivia`)
2. Register players via the join endpoint (`/app/quiz/join`)
3. Start the quiz when all players are ready (`/app/quiz/start`)
4. Process questions and submit answers (`/app/quiz/submit`)
5. Handle results and end-of-quiz events (`/app/quiz/end`)

## API Reference

The Endurance service provides a WebSocket-only API for complete quiz management and real-time interaction.

### WebSocket Message Destinations

#### Quiz Management

| Destination | Description | Payload |
|-------------|-------------|---------|
| `/app/quiz/create` | Create a new quiz | Quiz object |
| `/app/quiz/create/trivia` | Create AI-generated trivia quiz | TriviaQuizRequest |
| `/app/quiz/join` | Join a quiz as a player | JoinQuizRequest |
| `/app/quiz/start` | Start a quiz | Quiz ID |
| `/app/quiz/submit` | Submit an answer | AnswerSubmission |
| `/app/quiz/pause` | Pause a quiz | Quiz ID |
| `/app/quiz/end` | End a quiz | Quiz ID |

#### Quiz Creation Example
```javascript
// Create a regular quiz
stompClient.send('/app/quiz/create', {}, JSON.stringify({
  title: "Financial Literacy Quiz",
  questions: [
    {
      questionText: "What is the capital of France?",
      options: ["London", "Paris", "Berlin", "Madrid"],
      correctOptionIndex: 1,
      points: 5
    }
  ],
  timePerQuestionInSeconds: 30
}));

// Create AI-generated trivia quiz
stompClient.send('/app/quiz/create/trivia', {}, JSON.stringify({
  title: "Dave Ramsey Financial Quiz",
  questionCount: 10,
  difficulty: "medium"
}));
```

#### Player Join Example
```javascript
stompClient.send('/app/quiz/join', {}, JSON.stringify({
  player: {
    id: "player123",
    name: "John Doe",
    score: 0,
    isReady: true
  },
  quizId: 1
}));
```

### WebSocket API

Connect to the WebSocket endpoint at `/quiz-websocket` using SockJS or a native WebSocket client.

#### Subscription Topics

| Topic | Description |
|-------|-------------|
| `/topic/quiz/state/{quizId}` | Quiz state updates |
| `/topic/quiz/players/{quizId}` | Player list updates |
| `/topic/quiz/question/{quizId}` | Current question |
| `/topic/quiz/results/{quizId}` | Question results |

#### WebSocket Connection Example
```javascript
// Using SockJS and STOMP
const socket = new SockJS('/quiz-websocket');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    // Subscribe to topics
    stompClient.subscribe('/topic/quiz/state/' + quizId, function(response) {
        const quizState = JSON.parse(response.body);
        // Handle quiz state update
    });

    stompClient.subscribe('/topic/quiz/players/' + quizId, function(response) {
        const players = JSON.parse(response.body);
        // Handle player list update
    });
});
```

#### Join Quiz Example
```javascript
stompClient.send("/app/quiz/join", {}, JSON.stringify({
    id: "playerId",
    name: "Player Name",
    score: 0,
    isReady: true
}));
```

#### Submit Answer Example
```javascript
stompClient.send("/app/quiz/submit", {}, JSON.stringify({
    playerId: "playerId",
    quizId: "quizId",
    questionId: 1,
    selectedOption: 2,
    submissionTime: Date.now()
}));
```

For complete API documentation, refer to the [API Reference Documentation](docs/api-reference.md).

## Security

- WebSocket connections are secured using STOMP headers
- Player sessions are managed via unique IDs
- Input validation on all submissions

## Development

### Building from Source

```bash
git clone https://github.com/SnapPetal/Endurance.git
cd endurance
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
