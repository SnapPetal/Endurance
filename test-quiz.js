const SockJS = require('sockjs-client');
const Stomp = require('stompjs');

console.log('Connecting to WebSocket...');
const socket = new SockJS('http://localhost:8080/quiz-websocket');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('✅ Connected to WebSocket');

    // Subscribe to quiz created topic
    stompClient.subscribe('/topic/quiz/created', function (message) {
        const quiz = JSON.parse(message.body);
        console.log('\n✅ Quiz Created Successfully:');
        console.log('   ID:', quiz.id);
        console.log('   Title:', quiz.title);
        console.log('   Questions:', quiz.questions.length);
        console.log('   Status:', quiz.status);

        if (quiz.questions.length > 0) {
            console.log('\n   First Question:', quiz.questions[0].questionText);
        }

        // Test starting the quiz
        console.log('\n📤 Attempting to start quiz...');
        const quizId = quiz.id;

        // Subscribe to quiz state updates
        stompClient.subscribe('/topic/quiz/state/' + quizId, function (stateMsg) {
            const state = JSON.parse(stateMsg.body);
            console.log('\n✅ Quiz Started Successfully!');
            console.log('   Current Question Index:', state.currentQuestionIndex);
            console.log('   Current Question:', state.currentQuestion.questionText);
            console.log('   Options:', state.currentQuestion.options);

            // Disconnect after success
            setTimeout(() => {
                stompClient.disconnect();
                console.log('\n✅ All tests passed! Application is working correctly.');
                process.exit(0);
            }, 1000);
        });

        // Start the quiz
        stompClient.send("/app/quiz/start", {}, JSON.stringify(quizId));
    });

    // Create a trivia quiz
    console.log('📤 Creating trivia quiz...');
    const request = {
        title: 'Test Trivia Quiz',
        questionCount: 3,
        difficulty: 'EASY'
    };

    stompClient.send("/app/quiz/create/trivia", {}, JSON.stringify(request));

}, function(error) {
    console.error('❌ Connection failed:', error);
    process.exit(1);
});