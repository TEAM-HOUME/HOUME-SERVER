package or.sopt.houme.compare.application.dto;

import or.sopt.houme.compare.domain.ComparePresetDetail;

import java.util.List;

public record PresetDetailResponse(
        PresetOriginalProductResponse originalProduct,
        List<PresetSimilarProductResponse> similarProducts,
        long totalCount
) {
    public static PresetDetailResponse from(ComparePresetDetail detail) {
        List<PresetSimilarProductResponse> items = detail.similarProducts().stream()
                .map(i -> new PresetSimilarProductResponse(
                        i.source(), i.productId(), i.title(), i.imageUrl(),
                        i.price(), i.currency(), i.siteName(), i.productUrl(), i.priceUpdatedAt()
                ))
                .toList();
        return new PresetDetailResponse(
                new PresetOriginalProductResponse(
                        detail.sourceUrl(), detail.title(), detail.thumbnailUrl(),
                        detail.brand(), detail.price(), detail.currency()
                ),
                items,
                items.size()
        );
    }
}
