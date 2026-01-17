package or.sopt.houme.domain.user.presentation.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import or.sopt.houme.domain.user.presentation.valid.ValidBirthday;

public record SocialSignUpRequest(
        @NotBlank(message = "signupToken은 필수 입력값입니다.")
        String signupToken,

        @NotBlank(message = "이름은 필수 입력값입니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "숫자, 특수문자는 입력할 수 없어요.")
        String name,

        @NotBlank(message = "성별은 필수 입력값입니다.")
        @Pattern(
                regexp = "MALE|FEMALE|NONBINARY",
                message = "성별은 MALE, FEMALE, NONBINARY 중 하나여야 해요."
        )
        String gender,

        @NotBlank(message = "생년월일은 필수 입력값입니다.")
        @ValidBirthday
        String birthday
) {
}

