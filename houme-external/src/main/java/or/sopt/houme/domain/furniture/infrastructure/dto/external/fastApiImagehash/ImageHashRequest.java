package or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;

import java.util.List;

public record ImageHashRequest(
        String baseImageUrl,
        List<ImageHashRequest.Product> products
) {
    public static ImageHashRequest of(String baseImageUrl, List<NaverFurnitureProductDto> products) {
        return new ImageHashRequest(
                baseImageUrl,
                products.stream()
                        .map(p -> new Product(
                                p.furnitureProductImageUrl(),
                                p.furnitureProductId()
                        ))
                        .toList()
        );
    }

    public record Product(
            String imageUrl,
            Long productId
    ) {}
}
