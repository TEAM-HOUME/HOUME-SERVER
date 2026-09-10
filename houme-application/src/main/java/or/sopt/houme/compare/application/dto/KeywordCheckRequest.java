package or.sopt.houme.compare.application.dto;

import jakarta.validation.constraints.NotBlank;

public record KeywordCheckRequest(@NotBlank String productName) {}
