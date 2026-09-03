package or.sopt.houme.compare.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record SimilarProductRequest(
        @NotBlank String source,
        @NotBlank String productId,
        @NotBlank String title,
        String imageUrl,
        @NotNull Double price,
        @NotBlank String currency,
        String siteName,
        @NotBlank String productUrl,
        @NotNull OffsetDateTime priceUpdatedAt
) {}
