package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import java.util.List;

public record RelatedImagesResponse(
        String name,
        List<RelatedImageResponse> images
) {

    public static RelatedImagesResponse of(String name, List<RelatedImageResponse> images) {
        return new RelatedImagesResponse(name, images);
    }
}
