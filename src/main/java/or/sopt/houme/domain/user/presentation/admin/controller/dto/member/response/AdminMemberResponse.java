package or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response;

public record AdminMemberResponse(
        Long memberId,
        String nickname,
        String nicknameTag,
        String email,
        long creditBalance
) {
}
