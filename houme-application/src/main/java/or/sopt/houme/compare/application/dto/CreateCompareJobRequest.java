package or.sopt.houme.compare.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCompareJobRequest(
        @NotBlank(message = "상품 URL은 필수입니다.")
        String url
) {}
