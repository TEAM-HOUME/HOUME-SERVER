package or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response;

import java.util.List;

public record AdminStyleListResponse(
        List<AdminStyleResponse> styles
) {
}
