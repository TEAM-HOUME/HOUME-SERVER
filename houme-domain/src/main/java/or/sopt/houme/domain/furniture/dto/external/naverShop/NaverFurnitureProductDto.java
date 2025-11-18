package or.sopt.houme.domain.furniture.dto.external.naverShop;

import io.swagger.v3.oas.annotations.media.Schema;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

import java.util.Map;
import java.util.Objects;

import static or.sopt.houme.global.util.HtmlTextCleaner.clean;

public record NaverFurnitureProductDto(

        @Schema(description = "추천 가구 이미지 url")
        String furnitureProductImageUrl,

        @Schema(description = "추천 가구 구매 사이트 url")
        String furnitureProductSiteUrl,

        @Schema(description = "추천 가구명")
        String furnitureProductName,

        @Schema(description = "추천 가구 판매 회사")
        String furnitureProductMallName,

        @Schema(description = "추천 가구 식별자")
        Long furnitureProductId

) {
    public static NaverFurnitureProductDto from(Map<String, Object> it) {
        return new NaverFurnitureProductDto(
                safeToString(it.get("image")),
                safeToString(it.get("link")),
                clean(safeToString(it.get("title"))),
                safeToString(it.get("mallName")),
                parseLongSafely(it.get("productId"))
        );
    }

    // productId를 Long으로 파싱, + 데이터 파싱 에러 핸들링
    private static Long parseLongSafely(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new GeneralException(ErrorCode.NAVER_API_DATA_PARSE_ERROR);
        }
    }

    /**
     * Object → String 안전 변환
     * null이면 null 반환
     */
    private static String safeToString(Object obj) {
        return Objects.toString(obj, null);
    }
}
