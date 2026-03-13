package or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request;

import java.util.List;

public record AdminStyleUpdateRequest(
        String bannerImageUrl,
        String bannerTitle,
        String styleDescription,
        String stylePrompt,
        List<Long> mappedRawProductIds
) {
}
