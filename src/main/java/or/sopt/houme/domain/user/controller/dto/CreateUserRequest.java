package or.sopt.houme.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import or.sopt.houme.domain.user.entity.Gender;
import or.sopt.houme.domain.user.valid.ValidBirthday;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank(message = "이름은 필수 입력값입니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "숫자, 특수문자는 입력할 수 없어요.")
        String name,

        @NotBlank(message = "성별은 필수 입력값입니다.")
        Gender gender,

        @NotBlank(message = "생년월일은 필수 입력값입니다.")
        @ValidBirthday
        LocalDate birthday
) {
    public static CreateUserRequest of(String name, Gender gender, LocalDate birthday) {
        return new CreateUserRequest(name, gender, birthday);
    }
}
