package or.sopt.houme.domain.user.controller.dto;

public record MyPageInfoResponse(
        Long userId,
        String name,
        Long CreditCount
) {
    public static MyPageInfoResponse of(Long userId, String name, Long creditCount) {
        return new MyPageInfoResponse(userId, name, creditCount);
    }
}
