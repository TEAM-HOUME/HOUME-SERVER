package or.sopt.houme.domain.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.entity.Role;


public record AdminSignUpRequestDTO(

        @Schema(description = "회원 ID 입니다")
        String username,

        @Schema(description = "회원 비밀번호 입니다")
        String password) {
}
