package or.sopt.houme.domain.address.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AddressRequestTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 주소요청이면 에러가없다")
    void shouldNotHaveViolations_WhenValidAddressRequest() {
        // Given
        AddressRequest request = new AddressRequest("서울특별시", "하우미로 123");

        // When
        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("시군구가 빈문자열이면 에러발생")
    void shouldHaveViolation_WhenSigunguIsBlank() {
        // Given
        AddressRequest request = new AddressRequest("  ", "하우미로");

        // When
        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sigungu"));
    }

    @Test
    @DisplayName("도로명이 null이면 에러발생")
    void shouldHaveViolation_WhenRoadNameIsNull() {
        // Given
        AddressRequest request = new AddressRequest("서울특별시 종로구", null);

        // When
        Set<ConstraintViolation<AddressRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("roadName"));
    }
}
