package or.sopt.houme.domain.user.controller.dto;

public record KakaoLoginResponse(
        boolean isNewUser,
        String signupToken,
        Prefill prefill
) {
    public static KakaoLoginResponse newUser(String signupToken, String email, String nickname) {
        return new KakaoLoginResponse(true, signupToken, new Prefill(email, nickname));
    }

    public static KakaoLoginResponse existingUser() {
        return new KakaoLoginResponse(false, null, null);
    }

    public record Prefill(
            String email,
            String nickname
    ) {
    }
}

