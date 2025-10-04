package or.sopt.houme.domain.furniture.dto.external.naverShop;

import java.util.List;
import java.util.Map;

public record NaverShopResponse(
        String lastBuildDate,
        int total,
        int start,
        int display,
        List<Map<String, Object>> items
) {}
