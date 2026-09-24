package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record JjymV2ItemResponse(
        String source,
        Long rawProductId,
        Long catalogItemId,
        boolean isJjym,
        String productImageUrl,
        String productSiteUrl,
        List<String> colors,
        String brandName,
        String productName,
        Long listPrice,
        Integer discountRate,
        Long discountPrice,
        Long jjymCount
) {
    public static JjymV2ItemResponse of(
            String source,
            Long rawProductId,
            Long catalogItemId,
            boolean isJjym,
            String productImageUrl,
            String productSiteUrl,
            List<String> colors,
            String brandName,
            String productName,
            Long listPrice,
            Integer discountRate,
            Long discountPrice,
            Long jjymCount
    ) {
        return new JjymV2ItemResponse(
                source,
                rawProductId,
                catalogItemId,
                isJjym,
                productImageUrl,
                productSiteUrl,
                colors,
                brandName,
                productName,
                listPrice,
                discountRate,
                discountPrice,
                jjymCount
        );
    }
}
