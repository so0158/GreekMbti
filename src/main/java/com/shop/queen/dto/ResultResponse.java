package com.shop.queen.dto;

import java.util.List;

public record ResultResponse(
    String mbtiType,
    String characterName,
    String emoji,
    String imageUrl,
    String description,
    List<String> traits
) {
}
