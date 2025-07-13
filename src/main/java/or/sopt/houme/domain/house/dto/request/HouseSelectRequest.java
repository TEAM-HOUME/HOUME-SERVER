package or.sopt.houme.domain.house.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HouseSelectRequest(
        @NotBlank(message = "주거형태 옵션 선택은 필수입니다.")
        String housingType, // 선택한 주거형태 옵션
        @NotBlank(message = "공간구조 옵션 선택은 필수입니다")
        String roomType,    // 선택한 공간구조 옵션
        @NotBlank(message = "평형 옵션 선택은 필수입니다.")
        String areaType,    // 선택한 평형 옵션
        @NotNull(message = "유효선택 입력은 필수입니다.")
        Boolean isValid  // 유효한 선택인지
) {
}
