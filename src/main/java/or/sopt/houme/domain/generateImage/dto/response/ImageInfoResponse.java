package or.sopt.houme.domain.generateImage.dto.response;

public record ImageInfoResponse(
        long imageId,   // 이미지 식별자
        String imageUrl, // 이미지 주소
        boolean isMirror,   // 반전 여부
        String equilibrium, // 평형
        String houseForm,   // 주거정보 (오피스텔)
        String tagName,    // 스타일
        String name         // 이름
) {
    public static ImageInfoResponse of(long imageId, String imageUrl,
                                       boolean isMirror, String equilibrium,
                                       String houseForm, String tagName, String name) {
        return new ImageInfoResponse(imageId, imageUrl, isMirror, equilibrium, houseForm, tagName, name);
    }
}
