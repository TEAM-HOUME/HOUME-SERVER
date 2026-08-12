package or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record AdminStyleUpdateRequest(
        String bannerImageUrl,
        String bannerTitle,
        String styleDescription,
        String stylePrompt,
        List<@NotNull @Positive Long> mappedRawProductIds
) {
}
