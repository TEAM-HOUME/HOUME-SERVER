package or.sopt.houme.domain.user.presentation.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminLoginResponse(
        @Schema(description = "어드민 액세스 토큰. Authorization: Bearer {accessToken} 으로 사용")
        String accessToken,
        @Schema(description = "토큰 유효 시간(초). 리프레시 토큰이 없으므로 만료되면 다시 로그인해야 한다", example = "86400")
        long expiresIn
) {
}
