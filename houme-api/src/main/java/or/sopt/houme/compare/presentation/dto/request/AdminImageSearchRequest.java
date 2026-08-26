package or.sopt.houme.compare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminImageSearchRequest(
        @NotBlank String imageUrl,
        @NotNull Double priceKrw,
        String category
) {}
