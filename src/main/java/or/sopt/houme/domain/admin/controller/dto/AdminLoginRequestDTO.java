package or.sopt.houme.domain.admin.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequestDTO(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
