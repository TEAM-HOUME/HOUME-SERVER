package or.sopt.houme.domain.generateImage.presentation.dto.response;

public record GenerateImageV4Response(
        Long imageId,
        String imageUrl,
        boolean isMirror
) {
    public static GenerateImageV4Response of(Long imageId, String imageUrl, boolean isMirror) {
        return new GenerateImageV4Response(imageId, imageUrl, isMirror);
    }
}
