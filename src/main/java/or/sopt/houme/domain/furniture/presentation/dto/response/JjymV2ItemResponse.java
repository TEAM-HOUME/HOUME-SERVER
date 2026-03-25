package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record JjymV2ItemResponse(
        Long rawProductId,
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
            Long rawProductId,
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
                rawProductId,
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
