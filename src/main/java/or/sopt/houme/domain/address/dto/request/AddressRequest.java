package or.sopt.houme.domain.address.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank(message = "시/군/구는 필수 입력값입니다.")
        String sigungu,
        @NotBlank(message = "도로명 주소는 필수 입력값입니다.")
        String roadName
) {
}
