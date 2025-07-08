package or.sopt.houme.domain.user.controller.dto;

public record MyPageInfoResponse(
        String name, Long CreditCount
) {
    public static MyPageInfoResponse of(String name, Long creditCount) {
        return new MyPageInfoResponse(name, creditCount);
    }
}
