package or.sopt.houme.compare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminTextSearchRequest(
        @NotBlank(message = "상품명은 필수입니다.") String title,
        String imageUrl,
        @NotNull(message = "가격은 필수입니다.") @Positive(message = "가격은 0보다 커야 합니다.") Double priceKrw,
        String category
) {}
