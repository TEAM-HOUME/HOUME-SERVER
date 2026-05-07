package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

public record GeneratedImageMetaResponse(
        Long imageId,
        String imageUrl,
        boolean isMirror
) {

    public static GeneratedImageMetaResponse of(Long imageId, String imageUrl, boolean isMirror) {
        return new GeneratedImageMetaResponse(imageId, imageUrl, isMirror);
    }
}
