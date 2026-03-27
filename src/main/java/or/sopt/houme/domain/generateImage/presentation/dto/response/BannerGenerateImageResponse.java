package or.sopt.houme.domain.generateImage.presentation.dto.response;

public record BannerGenerateImageResponse(
        Long imageId
) {
    public static BannerGenerateImageResponse of(Long imageId) {
        return new BannerGenerateImageResponse(imageId);
    }
}
