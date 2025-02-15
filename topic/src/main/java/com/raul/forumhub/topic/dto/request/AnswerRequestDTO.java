package com.raul.forumhub.topic.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequestDTO(@NotBlank(message = "A solução não pode ser vazia") String solution) {
}
