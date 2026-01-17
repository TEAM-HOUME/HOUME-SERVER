package or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash.forPlan;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;

import java.util.List;

public record ImageHashRequestForPlan(
        String baseImageUrl,
        List<ImageHashRequestForPlan.Product> products,
        int pHash,
        int colorHash
) {
    public static ImageHashRequestForPlan of(
            String baseImageUrl, List<NaverFurnitureProductDto> products, int pHash, int colorHash
    ) {
        return new ImageHashRequestForPlan(
                baseImageUrl,
                products.stream()
                        .map(p -> new ImageHashRequestForPlan.Product(
                                p.furnitureProductImageUrl(),
                                p.furnitureProductId()
                        ))
                        .toList(),
                pHash, colorHash
        );
    }

    public record Product(
            String imageUrl,
            Long productId
    ) {}
}
