package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import java.util.List;

public record GenerateImageResultResponse(
        Long imageId,
        List<GenerateImageResultProductResponse> products
) {

    public static GenerateImageResultResponse of(
            Long imageId,
            List<GenerateImageResultProductResponse> products
    ) {
        return new GenerateImageResultResponse(imageId, products);
    }
}
