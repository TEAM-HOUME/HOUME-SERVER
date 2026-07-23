package or.sopt.houme.domain.user.presentation.controller.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import or.sopt.houme.domain.user.presentation.valid.ValidBirthday;

public record UpdateMyPageProfileRequest(
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "특수문자는 입력할 수 없어요.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
        String nickname,

        @Pattern(
                regexp = "MALE|FEMALE|NONBINARY",
                message = "성별은 MALE, FEMALE, NONBINARY 중 하나여야 해요."
        )
        String gender,

        @ValidBirthday
        String birthday
) {
}
