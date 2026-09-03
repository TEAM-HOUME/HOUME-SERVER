package or.sopt.houme.compare.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import or.sopt.houme.compare.domain.ComparePresetDetail;

import java.util.List;

public record SavePresetRequest(
        @NotBlank String sourceUrl,
        @NotBlank String title,
        String thumbnailUrl,
        String brand,
        Long price,
        @NotBlank String currency,
        @NotNull @Valid List<SimilarProductRequest> similarProducts
) {
    public ComparePresetDetail toDomain() {
        return new ComparePresetDetail(
                sourceUrl, title, thumbnailUrl, brand, price, currency,
                similarProducts.stream()
                        .map(i -> new ComparePresetDetail.SimilarItem(
                                i.source(), i.productId(), i.title(), i.imageUrl(),
                                i.price(), i.currency(), i.siteName(),
                                i.productUrl(), i.priceUpdatedAt()
                        ))
                        .toList()
        );
    }
}
