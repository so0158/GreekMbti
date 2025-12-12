// MBTI 카테고리 순서 - 같은 카테고리가 연속으로 나오지 않도록 섞음
const categories = ['EI', 'SN', 'TF', 'JP', 'EI', 'SN', 'TF', 'JP', 'EI', 'SN', 'TF', 'JP'];

// 현재 상태
let currentQuestion = 0;
let answers = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };
let currentQuestionData = null;
const totalQuestions = 12;

// 테스트 시작
async function startTest() {
    currentQuestion = 0;
    answers = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };
    showScreen('question-screen');
    await loadQuestion();
}

// 화면 전환
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(screen => {
        screen.classList.remove('active');
    });
    document.getElementById(screenId).classList.add('active');
}

// 질문 로딩
async function loadQuestion() {
    try {
        // 로딩 표시
        document.getElementById('question-text').textContent = '질문을 생성하는 중...';
        document.getElementById('answer-a').textContent = '잠시만 기다려주세요';
        document.getElementById('answer-b').textContent = '잠시만 기다려주세요';
        document.getElementById('answer-a').disabled = true;
        document.getElementById('answer-b').disabled = true;

        const category = categories[currentQuestion];

        const response = await fetch('/api/mbti/question', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                questionNumber: currentQuestion + 1,
                category: category
            })
        });

        if (!response.ok) {
            throw new Error('질문을 불러오는데 실패했습니다.');
        }

        currentQuestionData = await response.json();
        displayQuestion();
    } catch (error) {
        console.error('Error loading question:', error);
        alert('질문을 불러오는데 실패했습니다. 다시 시도해주세요.');
        document.getElementById('question-text').textContent = '질문을 불러오는데 실패했습니다.';
        document.getElementById('answer-a').textContent = '다시 시도';
        document.getElementById('answer-b').textContent = '처음으로';
        document.getElementById('answer-a').disabled = false;
        document.getElementById('answer-b').disabled = false;
    }
}

// 질문 표시
function displayQuestion() {
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
async function selectAnswer(choice) {
    const selectedAnswer = choice === 'A' ? currentQuestionData.answers[0] : currentQuestionData.answers[1];

    answers[selectedAnswer.type]++;

    currentQuestion++;

    if (currentQuestion < totalQuestions) {
        await loadQuestion();
    } else {
        await showResult();
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
function restartTest() {
    showScreen('start-screen');
}
