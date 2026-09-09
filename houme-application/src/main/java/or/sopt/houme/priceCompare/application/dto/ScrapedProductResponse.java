package or.sopt.houme.priceCompare.application.dto;

import or.sopt.houme.priceCompare.domain.ScrapedProduct;

import java.util.List;

/**
 * 스크래핑 결과 응답.
 * {@code quality} 는 부분 성공을 드러내기 위한 필드로, 프론트가 안내 문구를 분기하는 데 쓴다.
 */
public record ScrapedProductResponse(
        String sourceUrl,
        String title,
        String thumbnailUrl,
        String brand,
        Long price,
        String currency,
        List<String> additionalImageUrls,
        String description,
        String quality
) {

    public static ScrapedProductResponse from(ScrapedProduct product) {
        return new ScrapedProductResponse(
                product.sourceUrl(),
                product.title(),
                product.thumbnailUrl(),
                product.brand(),
                product.price(),
                product.currency(),
                product.additionalImageUrls(),
                product.description(),
                product.quality().name()
        );
    }
}
