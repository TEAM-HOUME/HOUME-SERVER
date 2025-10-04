package or.sopt.houme.domain.furniture.dto.external.naverShop;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

import java.util.Map;
import java.util.Objects;

import static or.sopt.houme.global.util.HtmlTextCleaner.clean;

public record NaverFurnitureProductDto(
        String furnitureProductImageUrl,
        String furnitureProductSiteUrl,
        String furnitureProductName,
        String furnitureProductMallName,
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
