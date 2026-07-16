package or.sopt.houme.domain.user.presentation.controller.dto;

public record MyPageInfoResponse(
        Long userId,
        String name,
        Long CreditCount,
        String email
) {
    public static MyPageInfoResponse of(Long userId, String name, Long creditCount, String email) {
        return new MyPageInfoResponse(userId, name, creditCount, email);
    }
}
