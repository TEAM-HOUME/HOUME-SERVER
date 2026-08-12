package or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response;

import java.util.List;

public record AdminLandingListResponse(
        List<AdminLandingResponse> landings
) {
}
