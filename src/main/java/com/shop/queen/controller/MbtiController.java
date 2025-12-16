package com.shop.queen.controller;

import com.shop.queen.dto.AnswerRequest;
import com.shop.queen.dto.QuestionRequest;
import com.shop.queen.dto.QuestionResponse;
import com.shop.queen.dto.ResultResponse;
import com.shop.queen.dto.AllQuestionsResponse;
import com.shop.queen.service.MbtiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mbti")
public class MbtiController {

    private final MbtiService mbtiService;

    public MbtiController(MbtiService mbtiService) {
        this.mbtiService = mbtiService;
    }

    @PostMapping("/start")
    public AllQuestionsResponse startTest() {
        return mbtiService.generateAllQuestions();
    }

    @PostMapping("/result")
    public ResultResponse getResult(@RequestBody AnswerRequest request) {
        return mbtiService.calculateResult(request.answers());
    }
}
