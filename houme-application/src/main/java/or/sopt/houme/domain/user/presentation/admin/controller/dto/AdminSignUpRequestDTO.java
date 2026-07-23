package or.sopt.houme.domain.user.presentation.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;


public record AdminSignUpRequestDTO(

        @Schema(description = "회원 ID 입니다")
        String username,

        @Schema(description = "회원 비밀번호 입니다")
        String password) {
}
