package or.sopt.houme.domain.user.presentation.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MyPageGeneratedImageV2Response(
        List<DateGroupResponse> groups
) {
    public static MyPageGeneratedImageV2Response of(List<DateGroupResponse> groups) {
        return new MyPageGeneratedImageV2Response(groups);
    }

    public record DateGroupResponse(
            LocalDate date,
            List<ItemResponse> items
    ) {
        public static DateGroupResponse of(LocalDate date, List<ItemResponse> items) {
            return new DateGroupResponse(date, items);
        }
    }

    public record ItemResponse(
            Long imageId,
            ViewType viewType,
            String generatedImageUrl,
            LocalDateTime generatedAt,
            @Schema(nullable = true)
            String bannerTitle,
            @Schema(nullable = true)
            String productSummaryText,
            boolean isMirror,
            List<UsedProductResponse> usedProducts
    ) {
        public static ItemResponse of(
                Long imageId,
                ViewType viewType,
                String generatedImageUrl,
                LocalDateTime generatedAt,
                String bannerTitle,
                String productSummaryText,
                boolean isMirror,
                List<UsedProductResponse> usedProducts
        ) {
            return new ItemResponse(
                    imageId,
                    viewType,
                    generatedImageUrl,
                    generatedAt,
                    bannerTitle,
                    productSummaryText,
                    isMirror,
                    usedProducts
            );
        }
    }

    public record UsedProductResponse(
            Long rawProductId,
            String productImageUrl,
            List<String> colors,
            String productName,
            Long listPrice,
            Integer discountRate,
            Long discountPrice,
            String productSiteUrl,
            Boolean isJjym
    ) {
        public static UsedProductResponse of(
                Long rawProductId,
                String productImageUrl,
                List<String> colors,
                String productName,
                Long listPrice,
                Integer discountRate,
                Long discountPrice,
                String productSiteUrl,
                Boolean isJjym
        ) {
            return new UsedProductResponse(
                    rawProductId,
                    productImageUrl,
                    colors,
                    productName,
                    listPrice,
                    discountRate,
                    discountPrice,
                    productSiteUrl,
                    isJjym
            );
        }
    }

    public enum ViewType {
        BANNER,
        STYLE,
        PRODUCT,
        FULL_FUNNEL,
        LEGACY
    }
}
