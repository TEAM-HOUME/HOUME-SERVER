package or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response;

public record AdminCreditGrantResponse(
        Long memberId,
        int grantedAmount,
        long creditBalance
) {
}
