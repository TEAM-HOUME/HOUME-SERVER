package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import java.util.List;

public record GenerateImageResultResponse(
        Long imageId,
        String imageUrl,
        boolean isMirror,
        List<GenerateImageResultProductResponse> products
) {

    public static GenerateImageResultResponse of(
            Long imageId,
            String imageUrl,
            boolean isMirror,
            List<GenerateImageResultProductResponse> products
    ) {
        return new GenerateImageResultResponse(imageId, imageUrl, isMirror, products);
    }
}
