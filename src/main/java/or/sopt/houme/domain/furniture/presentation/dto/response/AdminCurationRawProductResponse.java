package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.time.LocalDateTime;

public record AdminCurationRawProductResponse(
        Long id,
        String source,
        SoozipCategory category,
        Long productId,
        String productImageUrl,
        String productSiteUrl,
        String productName,
        String productMallName,
        String brand,
        Long listPrice,
        Integer discountRate,
        Long discountPrice,
        Long baseShippingFee,
        Long freeShippingCondition,
        LocalDateTime fetchedAt
) {
    public static AdminCurationRawProductResponse of(CurationRawProduct rawProduct) {
        return new AdminCurationRawProductResponse(
                rawProduct.getId(),
                rawProduct.getSource(),
                rawProduct.getCategory(),
                rawProduct.getProductId(),
                rawProduct.getProductImageUrl(),
                rawProduct.getProductSiteUrl(),
                rawProduct.getProductName(),
                rawProduct.getProductMallName(),
                rawProduct.getBrand(),
                rawProduct.getListPrice(),
                rawProduct.getDiscountRate(),
                rawProduct.getDiscountPrice(),
                rawProduct.getBaseShippingFee(),
                rawProduct.getFreeShippingCondition(),
                rawProduct.getFetchedAt()
        );
    }
}
