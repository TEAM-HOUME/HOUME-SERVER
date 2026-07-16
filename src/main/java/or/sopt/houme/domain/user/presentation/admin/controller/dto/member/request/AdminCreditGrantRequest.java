package or.sopt.houme.domain.user.presentation.admin.controller.dto.member.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminCreditGrantRequest(
        @NotNull @Positive @Max(1000) Integer amount
) {
}
