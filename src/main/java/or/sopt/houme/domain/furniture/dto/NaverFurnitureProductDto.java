package or.sopt.houme.domain.furniture.dto;

import java.util.Map;

public record NaverFurnitureProductDto(
        String furnitureProductImageUrl,
        String furnitureProductSiteUrl,
        String furnitureProductName,
        String furnitureProductMallName
) {
    public static NaverFurnitureProductDto from(Map<String, Object> it) {
        return new NaverFurnitureProductDto(
                (String) it.get("image"),
                (String) it.get("link"),
                cleanTitle((String) it.get("title")),
                (String) it.get("mallName")
        );
    }

    // 아래 예시와 같은 태그를 제거하여 문자열을 정제합니다.
    // 예시: THE PI 원형 <b>러그</b> 160CM <b>핑크색</b> 160CM<b>러그</b> 원형<b>러그</b> 거실<b>러그</b>
    private static String cleanTitle(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("(?i)</?b>", "")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"");
    }
}
