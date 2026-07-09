package or.sopt.houme.domain.generateImage.presentation.dto.response;

public record OtherStyleGenerateImageResponse(
        Long imageId,
        String imageUrl,
        boolean isMirror
) {
    public static OtherStyleGenerateImageResponse of(Long imageId, String imageUrl, boolean isMirror) {
        return new OtherStyleGenerateImageResponse(imageId, imageUrl, isMirror);
    }
}
