// 현재 상태
let currentQuestion = 0;
let answers = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };
let allQuestions = []; // 테스트 시작 시 생성된 모든 질문 저장
let questionsLoaded = false; // 질문이 로드되었는지 확인
const totalQuestions = 20;

// 페이지 로딩 시 자동으로 질문 생성
window.addEventListener('DOMContentLoaded', async () => {
    await loadAllQuestions();
});

// 테스트 시작
function startTest() {
    // 질문이 로드되지 않았다면 실행하지 않음 (버튼이 표시되지 않으므로 이 코드는 실행되지 않아야 함)
    if (!questionsLoaded) {
        return;
    }

    currentQuestion = 0;
    answers = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };
    showScreen('question-screen');

    // 첫 번째 질문 표시
    if (allQuestions.length > 0) {
        displayQuestion();
    }
}

// 20개 질문을 한 번에 로딩
async function loadAllQuestions() {
    try {
        console.log('질문 생성 시작...');

        const response = await fetch('/api/mbti/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error('질문을 불러오는데 실패했습니다.');
        }

        const data = await response.json();
        allQuestions = data.questions;
        questionsLoaded = true;
        console.log('질문 생성 완료! 총 ' + allQuestions.length + '개');

        // 버튼 표시 및 로딩 메시지 숨기기
        const startButton = document.getElementById('start-button');
        const loadingMessage = document.getElementById('loading-message');
        if (startButton) startButton.style.display = 'inline-block';
        if (loadingMessage) loadingMessage.style.display = 'none';

    } catch (error) {
        console.error('Error loading questions:', error);
        questionsLoaded = false;

        // 에러 메시지 표시
        const loadingMessage = document.getElementById('loading-message');
        if (loadingMessage) {
            loadingMessage.textContent = '질문 생성에 실패했습니다. 페이지를 새로고침 해주세요.';
            loadingMessage.style.color = '#e74c3c';
        }
    }
}

// 화면 전환
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(screen => {
        screen.classList.remove('active');
    });
    document.getElementById(screenId).classList.add('active');
}

// 질문 표시
function displayQuestion() {
    const currentQuestionData = allQuestions[currentQuestion];

    document.getElementById('current-question').textContent = currentQuestion + 1;
    document.getElementById('total-questions').textContent = totalQuestions;
    document.getElementById('question-text').textContent = currentQuestionData.question;

    const answerA = document.getElementById('answer-a');
    const answerB = document.getElementById('answer-b');

    answerA.textContent = currentQuestionData.answers[0].text;
    answerB.textContent = currentQuestionData.answers[1].text;

    answerA.disabled = false;
    answerB.disabled = false;

    // 진행률 업데이트
    const progress = ((currentQuestion + 1) / totalQuestions) * 100;
    document.getElementById('progress').style.width = progress + '%';
}

// 답변 선택
function selectAnswer(choice) {
    const currentQuestionData = allQuestions[currentQuestion];
    const selectedAnswer = choice === 'A' ? currentQuestionData.answers[0] : currentQuestionData.answers[1];

    answers[selectedAnswer.type]++;

    currentQuestion++;

    if (currentQuestion < totalQuestions) {
        displayQuestion();
    } else {
        showResult();
    }
}

// 결과 계산 및 표시
async function showResult() {
    try {
        // 로딩 표시
        showScreen('result-screen');
        document.getElementById('mbti-type').textContent = '계산 중...';
        document.getElementById('character-name').textContent = '결과를 분석하는 중입니다';
        document.getElementById('character-description').textContent = '잠시만 기다려주세요...';

        const response = await fetch('/api/mbti/result', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                answers: answers
            })
        });

        if (!response.ok) {
            throw new Error('결과를 불러오는데 실패했습니다.');
        }

        const result = await response.json();

        document.getElementById('mbti-type').textContent = result.mbtiType;
        document.getElementById('character-name').textContent = result.characterName;

        // 이미지 표시 (emoji는 폴백으로 사용)
        const imageContainer = document.getElementById('character-image');
        if (result.imageUrl) {
            imageContainer.innerHTML = `<img src="${result.imageUrl}" alt="${result.characterName}" class="character-img" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                                       <span class="emoji-fallback" style="display:none;">${result.emoji}</span>`;
        } else {
            imageContainer.textContent = result.emoji;
        }

        document.getElementById('character-description').textContent = result.description;

        const traitsList = document.getElementById('character-traits-list');
        traitsList.innerHTML = '';
        result.traits.forEach(trait => {
            const li = document.createElement('li');
            li.textContent = trait;
            traitsList.appendChild(li);
        });
    } catch (error) {
        console.error('Error loading result:', error);
        document.getElementById('mbti-type').textContent = 'ERROR';
        document.getElementById('character-name').textContent = '결과를 불러오는데 실패했습니다';
        document.getElementById('character-description').textContent = '다시 시도해주세요.';
    }
}

// 테스트 재시작
async function restartTest() {
    showScreen('start-screen');

    // 버튼 숨기기 및 로딩 메시지 표시
    const startButton = document.getElementById('start-button');
    const loadingMessage = document.getElementById('loading-message');
    if (startButton) startButton.style.display = 'none';
    if (loadingMessage) {
        loadingMessage.style.display = 'block';
        loadingMessage.textContent = '새로운 질문을 생성하는 중입니다... (약 30초 소요)';
        loadingMessage.style.color = '#888';
    }

    // 새로운 질문 세트 생성
    await loadAllQuestions();
}
