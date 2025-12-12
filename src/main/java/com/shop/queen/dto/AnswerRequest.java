package com.shop.queen.dto;

import java.util.Map;

public record AnswerRequest(
    Map<String, Integer> answers
) {
}
