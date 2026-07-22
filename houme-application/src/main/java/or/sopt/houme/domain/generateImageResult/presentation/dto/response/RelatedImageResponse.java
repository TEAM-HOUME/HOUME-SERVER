package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

public record RelatedImageResponse(
        Long id,
        String imageUrl,
        String resultType
) {

    public static RelatedImageResponse of(Long id, String imageUrl, String resultType) {
        return new RelatedImageResponse(id, imageUrl, resultType);
    }
}
