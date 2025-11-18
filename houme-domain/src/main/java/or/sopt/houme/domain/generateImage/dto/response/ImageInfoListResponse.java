package or.sopt.houme.domain.generateImage.dto.response;

import java.util.List;

public record ImageInfoListResponse(
        List<ImageInfoResponse> imageInfoResponses
) {
    public static ImageInfoListResponse of(List<ImageInfoResponse> imageInfoResponses){
        return new ImageInfoListResponse(imageInfoResponses);
    }
}
