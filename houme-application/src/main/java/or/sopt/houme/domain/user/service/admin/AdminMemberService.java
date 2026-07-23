package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminCreditGrantResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminMemberSearchResponse;

public interface AdminMemberService {

    AdminMemberSearchResponse searchMembers(String keyword);

    AdminCreditGrantResponse grantCredits(Long memberId, int amount);
}
