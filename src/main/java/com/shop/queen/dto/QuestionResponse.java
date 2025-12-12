package com.shop.queen.dto;

import java.util.List;

public record QuestionResponse(
    String question,
    List<Answer> answers
) {
    public record Answer(
        String text,
        String type
    ) {}
}
