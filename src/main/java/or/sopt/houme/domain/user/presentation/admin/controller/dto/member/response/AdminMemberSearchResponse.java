package or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response;

import java.util.List;

public record AdminMemberSearchResponse(
        List<AdminMemberResponse> members
) {
}
