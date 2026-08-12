package or.sopt.houme.domain.generateImage.presentation.dto.response;

public record BannerGenerateImageResponse(
        Long imageId,
        String imageUrl,
        boolean isMirror
) {
    public static BannerGenerateImageResponse of(Long imageId, String imageUrl, boolean isMirror) {
        return new BannerGenerateImageResponse(imageId, imageUrl, isMirror);
    }
}
