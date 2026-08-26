package or.sopt.houme.compare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminTextSearchRequest(
        @NotBlank String title,
        String imageUrl,
        @NotNull Double priceKrw,
        String category
) {}
