package com.shop.queen.service;

import com.shop.queen.dto.QuestionResponse;
import com.shop.queen.dto.ResultResponse;
import com.shop.queen.dto.AllQuestionsResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MbtiService {

    private final ChatClient chatClient;

    public MbtiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // MBTI 카테고리별 질문 순서 (각 5개씩, 총 20개)
    private static final String[] CATEGORIES = {"EI", "SN", "TF", "JP", "EI", "SN", "TF", "JP", "EI", "SN", "TF", "JP", "EI", "SN", "TF", "JP", "EI", "SN", "TF", "JP"};

    // 테스트 시작 시 20개 질문을 한 번에 생성
    public AllQuestionsResponse generateAllQuestions() {
        String prompt = """
                You are an MBTI test expert creating questions for a personality test.

                [CRITICAL] You MUST respond ONLY in Korean.

                Create 20 MBTI questions following these rules:

                [REQUIREMENTS]
                1. Create general life situation questions (NOT Greek mythology themed)
                2. Questions should be about everyday life, work, relationships, hobbies, etc.
                3. All content MUST be in Korean
                4. Keep each question under 40 Korean characters
                5. Keep each answer under 50 Korean characters
                6. Make each question unique and different from typical MBTI questions

                [QUESTION DISTRIBUTION]
                - Questions 1, 5, 9, 13, 17: E vs I (외향 vs 내향)
                - Questions 2, 6, 10, 14, 18: S vs N (감각 vs 직관)
                - Questions 3, 7, 11, 15, 19: T vs F (사고 vs 감정)
                - Questions 4, 8, 12, 16, 20: J vs P (판단 vs 인식)

                Response Format (in Korean):
                질문1: [question] | 답변A: [answer] | 유형: E | 답변B: [answer] | 유형: I
                질문2: [question] | 답변A: [answer] | 유형: S | 답변B: [answer] | 유형: N
                질문3: [question] | 답변A: [answer] | 유형: T | 답변B: [answer] | 유형: F
                질문4: [question] | 답변A: [answer] | 유형: J | 답변B: [answer] | 유형: P
                ... (continue to 질문20)

                Example:
                질문1: 주말에 에너지를 충전하는 방법은? | 답변A: 친구들을 만나거나 사람들과 어울린다 | 유형: E | 답변B: 집에서 혼자만의 시간을 보낸다 | 유형: I
                질문2: 대화할 때 주로 | 답변A: 구체적인 사실과 경험을 이야기한다 | 유형: S | 답변B: 추상적인 개념이나 가능성을 이야기한다 | 유형: N
            """;

        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        return parseAllQuestionsResponse(response);
    }

    private AllQuestionsResponse parseAllQuestionsResponse(String response) {
        List<QuestionResponse> questions = new ArrayList<>();
        String[] lines = response.split("\n");

        for (int i = 0; i < lines.length && questions.size() < 20; i++) {
            String line = lines[i].trim();
            if (line.startsWith("질문")) {
                try {
                    // 질문1: [question] | 답변A: [answer] | 유형: E | 답변B: [answer] | 유형: I
                    String[] parts = line.split("\\|");

                    String question = parts[0].substring(parts[0].indexOf(":") + 1).trim();
                    String answerA = parts[1].substring(parts[1].indexOf(":") + 1).trim();
                    String typeA = parts[2].substring(parts[2].indexOf(":") + 1).trim();
                    String answerB = parts[3].substring(parts[3].indexOf(":") + 1).trim();
                    String typeB = parts[4].substring(parts[4].indexOf(":") + 1).trim();

                    List<QuestionResponse.Answer> answers = Arrays.asList(
                        new QuestionResponse.Answer(answerA, typeA),
                        new QuestionResponse.Answer(answerB, typeB)
                    );

                    questions.add(new QuestionResponse(question, answers));
                } catch (Exception e) {
                    System.err.println("Failed to parse line: " + line);
                }
            }
        }

        // 20개가 안 되면 기본 질문으로 채우기
        while (questions.size() < 20) {
            int idx = questions.size();
            String category = CATEGORIES[idx];
            String typeA = category.substring(0, 1);
            String typeB = category.substring(1, 2);

            questions.add(new QuestionResponse(
                "질문 " + (idx + 1),
                Arrays.asList(
                    new QuestionResponse.Answer("답변 A", typeA),
                    new QuestionResponse.Answer("답변 B", typeB)
                )
            ));
        }

        return new AllQuestionsResponse(questions);
    }

    public ResultResponse calculateResult(Map<String, Integer> answers) {
        String mbtiType =
            (answers.getOrDefault("E", 0) > answers.getOrDefault("I", 0) ? "E" : "I") +
            (answers.getOrDefault("S", 0) > answers.getOrDefault("N", 0) ? "S" : "N") +
            (answers.getOrDefault("T", 0) > answers.getOrDefault("F", 0) ? "T" : "F") +
            (answers.getOrDefault("J", 0) > answers.getOrDefault("P", 0) ? "J" : "P");

        return getGreekCharacter(mbtiType);
    }

    private ResultResponse getGreekCharacter(String mbtiType) {
        Map<String, ResultResponse> characters = new HashMap<>();

        characters.put("INTJ", new ResultResponse(
            "INTJ",
            "아테나 (Athena)",
            "🦉",
            null,
            "지혜와 전략의 여신 아테나처럼, 당신은 뛰어난 통찰력과 전략적 사고를 가진 사람입니다.",
            Arrays.asList("전략적이고 체계적인 계획가", "독립적이며 자기 확신이 강함", "지식과 능력 개발에 열정적", "혁신적인 해결책을 찾는 능력")
        ));

        characters.put("INTP", new ResultResponse(
            "INTP",
            "헤파이스토스 (Hephaestus)",
            "🔨",
            "https://image.pollinations.ai/prompt/Greek%20god%20Hephaestus%20blacksmith%20with%20forge%20and%20hammer%2C%20creative%20anime%20style%2C%20warm%20colors%2C%20detailed%20art?width=800&height=600&nologo=true",
            "대장장이의 신 헤파이스토스처럼, 당신은 창의적이고 논리적인 사고로 새로운 것을 만들어내는 발명가입니다.",
            Arrays.asList("분석적이고 논리적인 사고", "호기심이 많고 탐구적", "독창적인 아이디어 창출", "복잡한 문제 해결을 즐김")
        ));

        characters.put("ENTJ", new ResultResponse(
            "ENTJ",
            "제우스 (Zeus)",
            "⚡",
            "https://image.pollinations.ai/prompt/Powerful%20Greek%20god%20Zeus%20with%20lightning%20bolt%2C%20majestic%20anime%20style%2C%20royal%20blue%20colors%2C%20epic%20illustration?width=800&height=600&nologo=true",
            "신들의 왕 제우스처럼, 당신은 타고난 리더십과 결단력을 가진 지도자입니다.",
            Arrays.asList("타고난 리더십과 추진력", "장기적 비전 제시", "효율적이고 체계적인 조직 관리", "도전적인 목표를 향한 열정")
        ));

        characters.put("ENTP", new ResultResponse(
            "ENTP",
            "헤르메스 (Hermes)",
            "🪽",
            "https://image.pollinations.ai/prompt/Greek%20god%20Hermes%20messenger%20with%20winged%20sandals%2C%20dynamic%20anime%20style%2C%20bright%20colors%2C%20energetic%20illustration?width=800&height=600&nologo=true",
            "전령의 신 헤르메스처럼, 당신은 재치 있고 민첩한 사고로 상황에 빠르게 대응합니다.",
            Arrays.asList("빠른 사고와 재치있는 대화", "혁신적이고 창의적인 아이디어", "다양한 관점에서 문제 접근", "논리적 토론을 즐김")
        ));

        characters.put("INFJ", new ResultResponse(
            "INFJ",
            "아폴론 (Apollo)",
            "🎵",
            "https://image.pollinations.ai/prompt/Greek%20god%20Apollo%20with%20lyre%20and%20sun%2C%20artistic%20anime%20style%2C%20golden%20light%2C%20beautiful%20illustration?width=800&height=600&nologo=true",
            "예언과 예술의 신 아폴론처럼, 당신은 깊은 통찰력과 이상주의를 가진 선구자입니다.",
            Arrays.asList("깊은 통찰력과 직관", "이상주의적 비전 추구", "타인에 대한 깊은 이해", "창의적이고 예술적 감각")
        ));

        characters.put("INFP", new ResultResponse(
            "INFP",
            "페르세포네 (Persephone)",
            "🌸",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Persephone%20with%20flowers%20and%20spring%2C%20gentle%20anime%20style%2C%20pastel%20pink%20colors%2C%20dreamy%20illustration?width=800&height=600&nologo=true",
            "봄의 여신 페르세포네처럼, 당신은 순수하고 이상적인 가치를 추구하는 몽상가입니다.",
            Arrays.asList("이상주의적 가치관", "풍부한 감수성과 상상력", "진정성과 순수함 추구", "예술적 표현력")
        ));

        characters.put("ENFJ", new ResultResponse(
            "ENFJ",
            "헤라 (Hera)",
            "👑",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Hera%20queen%20with%20crown%20and%20peacock%2C%20regal%20anime%20style%2C%20royal%20purple%20colors%2C%20majestic%20illustration?width=800&height=600&nologo=true",
            "신들의 여왕 헤라처럼, 당신은 사람들을 이끌고 조화롭게 만드는 카리스마 있는 리더입니다.",
            Arrays.asList("뛰어난 공감 능력과 리더십", "타인의 성장을 돕는 멘토", "조화로운 관계 구축", "영감을 주는 의사소통")
        ));

        characters.put("ENFP", new ResultResponse(
            "ENFP",
            "아프로디테 (Aphrodite)",
            "💖",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Aphrodite%20of%20love%20and%20beauty%2C%20charming%20anime%20style%2C%20pink%20and%20gold%20colors%2C%20romantic%20illustration?width=800&height=600&nologo=true",
            "사랑과 아름다움의 여신 아프로디테처럼, 당신은 열정적이고 창의적인 에너지로 주변을 밝힙니다.",
            Arrays.asList("열정적이고 창의적", "사람들과의 깊은 교감", "새로운 경험에 대한 열린 마음", "긍정적 에너지 전파")
        ));

        characters.put("ISTJ", new ResultResponse(
            "ISTJ",
            "헤스티아 (Hestia)",
            "🔥",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Hestia%20of%20hearth%20and%20home%20with%20sacred%20flame%2C%20warm%20anime%20style%2C%20orange%20and%20red%20colors%2C%20cozy%20illustration?width=800&height=600&nologo=true",
            "가정의 여신 헤스티아처럼, 당신은 책임감 있고 신뢰할 수 있는 사람입니다.",
            Arrays.asList("책임감 있고 신뢰할 수 있음", "체계적이고 조직적", "전통과 규칙 존중", "꼼꼼하고 정확한 업무 처리")
        ));

        characters.put("ISFJ", new ResultResponse(
            "ISFJ",
            "데메테르 (Demeter)",
            "🌾",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Demeter%20of%20harvest%20with%20wheat%20and%20nature%2C%20nurturing%20anime%20style%2C%20earth%20tone%20colors%2C%20peaceful%20illustration?width=800&height=600&nologo=true",
            "수확의 여신 데메테르처럼, 당신은 헌신적이고 따뜻한 마음으로 타인을 돌보는 양육자입니다.",
            Arrays.asList("헌신적이고 배려심 깊음", "실용적이고 세심한 돌봄", "안정과 조화 추구", "타인의 필요를 먼저 생각")
        ));

        characters.put("ESTJ", new ResultResponse(
            "ESTJ",
            "아레스 (Ares)",
            "⚔️",
            "https://image.pollinations.ai/prompt/Greek%20god%20Ares%20of%20war%20with%20armor%20and%20sword%2C%20powerful%20anime%20style%2C%20red%20and%20black%20colors%2C%20warrior%20illustration?width=800&height=600&nologo=true",
            "전쟁의 신 아레스처럼, 당신은 강력한 추진력과 용기로 목표를 달성하는 실행가입니다.",
            Arrays.asList("강력한 실행력과 결단력", "체계적인 조직 관리", "효율성과 생산성 추구", "명확한 규칙과 질서 선호")
        ));

        characters.put("ESFJ", new ResultResponse(
            "ESFJ",
            "헤베 (Hebe)",
            "🌟",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Hebe%20of%20youth%20with%20cup%20of%20nectar%2C%20cheerful%20anime%20style%2C%20bright%20yellow%20colors%2C%20joyful%20illustration?width=800&height=600&nologo=true",
            "청춘의 여신 헤베처럼, 당신은 활기차고 사교적이며 타인을 돌보는 것을 즐깁니다.",
            Arrays.asList("사교적이고 친화력이 뛰어남", "타인을 돕고 배려함", "조화로운 분위기 조성", "전통과 사회적 가치 존중")
        ));

        characters.put("ISTP", new ResultResponse(
            "ISTP",
            "아르테미스 (Artemis)",
            "🏹",
            "https://image.pollinations.ai/prompt/Greek%20goddess%20Artemis%20hunter%20with%20bow%20and%20deer%2C%20independent%20anime%20style%2C%20silver%20and%20green%20colors%2C%20wild%20illustration?width=800&height=600&nologo=true",
            "사냥의 여신 아르테미스처럼, 당신은 독립적이고 실용적인 문제 해결사입니다.",
            Arrays.asList("독립적이고 자유로움", "실용적인 문제 해결", "순발력과 적응력", "위기 상황에서의 침착함")
        ));

        characters.put("ISFP", new ResultResponse(
            "ISFP",
            "뮤즈 (Muses)",
            "🎨",
            "https://image.pollinations.ai/prompt/Greek%20muse%20goddess%20of%20arts%20with%20painting%20and%20music%2C%20artistic%20anime%20style%2C%20rainbow%20colors%2C%20creative%20illustration?width=800&height=600&nologo=true",
            "예술의 여신 뮤즈처럼, 당신은 감수성이 풍부하고 예술적인 영혼을 가진 사람입니다.",
            Arrays.asList("예술적 감각과 심미안", "온화하고 배려심 깊음", "현재를 즐기는 여유", "자유로운 자기 표현")
        ));

        characters.put("ESTP", new ResultResponse(
            "ESTP",
            "포세이돈 (Poseidon)",
            "🌊",
            "https://image.pollinations.ai/prompt/Greek%20god%20Poseidon%20of%20sea%20with%20trident%20and%20waves%2C%20dynamic%20anime%20style%2C%20ocean%20blue%20colors%2C%20adventurous%20illustration?width=800&height=600&nologo=true",
            "바다의 신 포세이돈처럼, 당신은 역동적이고 대담한 모험가입니다.",
            Arrays.asList("역동적이고 에너지 넘침", "대담하고 모험적", "빠른 판단과 실행", "현실적이고 실용적")
        ));

        characters.put("ESFP", new ResultResponse(
            "ESFP",
            "디오니소스 (Dionysus)",
            "🍇",
            "https://image.pollinations.ai/prompt/Greek%20god%20Dionysus%20of%20wine%20and%20festivity%20with%20grapes%2C%20festive%20anime%20style%2C%20purple%20and%20green%20colors%2C%20party%20illustration?width=800&height=600&nologo=true",
            "축제의 신 디오니소스처럼, 당신은 즐거움과 열정으로 가득한 엔터테이너입니다.",
            Arrays.asList("활기차고 즐거움을 추구", "사교적이고 친근함", "순간을 즐기는 낙천성", "창의적인 즉흥성")
        ));

        return characters.getOrDefault(mbtiType, characters.get("ENFP"));
    }
}
