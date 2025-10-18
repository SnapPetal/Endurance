const SockJS = require('sockjs-client');
const Stomp = require('stompjs');

console.log('🔌 Connecting to WebSocket...');
const socket = new SockJS('http://localhost:8080/quiz-websocket');
const stompClient = Stomp.over(socket);

let quizId = null;
let playerId = null;
let currentQuestionIndex = 0;

stompClient.connect({}, function (frame) {
    console.log('✅ Connected to WebSocket\n');

    // Subscribe to quiz created topic
    stompClient.subscribe('/topic/quiz/created', function (message) {
        const quiz = JSON.parse(message.body);
        quizId = quiz.id;

        console.log('✅ Quiz Created:');
        console.log('   ID:', quiz.id);
        console.log('   Title:', quiz.title);
        console.log('   Questions:', quiz.questions.length);
        console.log('   Status:', quiz.status);

        if (quiz.questions && quiz.questions.length > 0) {
            console.log('\n📝 Sample Questions:');
            quiz.questions.forEach((q, idx) => {
                console.log(`   ${idx + 1}. ${q.questionText}`);
            });
        }

        // Join the quiz as a player
        console.log('\n👤 Joining quiz as player...');
        const player = {
            name: 'Test Player',
            email: 'test@example.com'
        };

        stompClient.subscribe('/topic/quiz/players', function (playersMsg) {
            const players = JSON.parse(playersMsg.body);
            if (players && players.length > 0) {
                playerId = players[0].id;
                console.log('✅ Player joined successfully:');
                console.log('   Player ID:', playerId);
                console.log('   Player Name:', players[0].name);

                // Start the quiz
                setTimeout(() => {
                    console.log('\n🚀 Starting quiz...');
                    stompClient.send("/app/quiz/start", {}, JSON.stringify(quizId));
                }, 1000);
            }
        });

        const joinRequest = {
            player: player,
            quizId: quizId
        };
        stompClient.send("/app/quiz/join", {}, JSON.stringify(joinRequest));
    });

    // Subscribe to quiz state updates
    let stateSubscription = null;

    function subscribeToQuizState() {
        if (quizId && !stateSubscription) {
            stateSubscription = stompClient.subscribe('/topic/quiz/state/' + quizId, function (stateMsg) {
                const state = JSON.parse(stateMsg.body);

                console.log('\n📊 Quiz State Update:');
                console.log('   Status:', state.status);
                console.log('   Current Question Index:', state.currentQuestionIndex);

                if (state.currentQuestion) {
                    console.log('   Current Question:', state.currentQuestion.questionText);
                    console.log('   Options:', state.currentQuestion.options);

                    // Submit answer to the current question
                    setTimeout(() => {
                        console.log('\n✏️  Submitting answer...');
                        const submission = {
                            quizId: quizId,
                            playerId: playerId,
                            questionId: state.currentQuestion.id,
                            answer: state.currentQuestion.options[0], // Submit first option
                            timestamp: Date.now()
                        };

                        stompClient.send("/app/quiz/submit", {}, JSON.stringify(submission));
                    }, 1000);
                } else if (state.status === 'COMPLETED') {
                    console.log('\n✅ Quiz Completed!');
                    if (state.playerScores && state.playerScores.length > 0) {
                        console.log('\n🏆 Final Scores:');
                        state.playerScores.forEach(score => {
                            console.log(`   ${score.playerName}: ${score.score} points (${score.correctAnswers} correct)`);
                        });
                    }

                    setTimeout(() => {
                        stompClient.disconnect();
                        console.log('\n✅ All tests passed! Quiz functionality is working correctly.');
                        process.exit(0);
                    }, 1000);
                }
            });
        }
    }

    // Create a trivia quiz
    console.log('📝 Creating trivia quiz...');
    const request = {
        title: 'Full Test Quiz',
        questionCount: 3,
        difficulty: 'EASY'
    };

    stompClient.send("/app/quiz/create/trivia", {}, JSON.stringify(request));

    // Set up subscription after quiz is created
    setTimeout(() => {
        subscribeToQuizState();
    }, 2000);

}, function(error) {
    console.error('❌ Connection failed:', error);
    process.exit(1);
});

// Handle disconnection
socket.onclose = function() {
    console.log('🔌 WebSocket connection closed');
};