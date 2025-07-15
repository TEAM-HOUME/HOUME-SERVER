package or.sopt.houme.domain.generateImage.dto.response;

public record ImageInfoResponse(
        long imageId,   // 이미지 식별자
        String imageUrl, // 이미지 주소
        boolean isMirror
) {
    public static ImageInfoResponse of(long imageId, String imageUrl, boolean isMirror) {
        return new ImageInfoResponse(imageId, imageUrl, isMirror);
    }
}
