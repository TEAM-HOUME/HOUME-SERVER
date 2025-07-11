package or.sopt.houme.domain.user.controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import or.sopt.houme.domain.user.entity.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 회원가입 요청인 경우면 성공")
    void validCreateUserRequest_shouldPassValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", Gender.MALE, LocalDate.now().minusYears(20));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름이 빈문자열인 경우 에러 발생")
    void blankName_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("", Gender.MALE, LocalDate.now().minusYears(20));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().equals("이름은 필수 입력값입니다."));
    }


    @Test
    @DisplayName("이름에 특수문자가 포함된 경우 에러 발생")
    void nameWithSpecialCharacters_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍*길동", Gender.MALE, LocalDate.now().minusYears(20));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().equals("숫자, 특수문자는 입력할 수 없어요."));
    }


    @Test
    @DisplayName("이름에 숫자가 포함된 경우 에러 발생")
    void nameWithNumber_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍1길2동", Gender.MALE, LocalDate.now().minusYears(20));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().equals("숫자, 특수문자는 입력할 수 없어요."));
    }

    @Test
    @DisplayName("성별이 null인 경우 에러 발생")
    void genderIsNull_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", null, LocalDate.now().minusYears(20));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("gender") &&
                        v.getMessage().equals("성별은 필수 입력값입니다."));
    }

    @Test
    @DisplayName("생년월일이 null인 경우 에러 발생")
    void birthdayIsNull_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", Gender.MALE, null);

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday") &&
                        v.getMessage().equals("생년월일은 필수 입력값입니다."));
    }

    @Test
    @DisplayName("나이가 만 14세 이하인 경우 에러 발생")
    void bunderFourteenYearsOld_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", Gender.MALE, LocalDate.now().minusYears(14).plusDays(1));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday") &&
                        v.getMessage().equals("올바른 생년월일을 입력해주세요."));
    }

    @Test
    @DisplayName("생년월일이 1900년 미만일 경우 에러 발생")
    void birthdayBefore1900_shouldFailValidation() {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", Gender.MALE, LocalDate.of(1899, 12, 31));

        // when
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday") &&
                        v.getMessage().equals("올바른 생년월일을 입력해주세요."));
    }
}
