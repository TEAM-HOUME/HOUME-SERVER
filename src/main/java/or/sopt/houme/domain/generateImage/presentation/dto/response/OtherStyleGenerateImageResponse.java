package or.sopt.houme.domain.generateImage.presentation.dto.response;

public record OtherStyleGenerateImageResponse(
        Long imageId
) {
    public static OtherStyleGenerateImageResponse of(Long imageId) {
        return new OtherStyleGenerateImageResponse(imageId);
    }
}
