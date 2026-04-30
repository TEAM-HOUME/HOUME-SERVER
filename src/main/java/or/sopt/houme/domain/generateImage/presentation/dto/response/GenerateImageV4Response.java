package or.sopt.houme.domain.generateImage.presentation.dto.response;

public record GenerateImageV4Response(
        Long imageId,
        String imageUrl
) {
    public static GenerateImageV4Response of(Long imageId, String imageUrl) {
        return new GenerateImageV4Response(imageId, imageUrl);
    }
}
