package or.sopt.houme.domain.user.entity.record;

public record SignupSession(
        Long kakaoId,
        String email,
        String nickname
) {
    public static SignupSession of(Long kakaoId, String email, String nickname) {
        return new SignupSession(kakaoId, email, nickname);
    }
}

