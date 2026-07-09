package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import java.util.List;

public record SimilarItemsResponse(
        List<SimilarItemResponse> products
) {

    public static SimilarItemsResponse of(List<SimilarItemResponse> products) {
        return new SimilarItemsResponse(products);
    }
}
