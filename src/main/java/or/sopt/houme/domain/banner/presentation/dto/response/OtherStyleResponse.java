package or.sopt.houme.domain.banner.presentation.dto.response;

import or.sopt.houme.domain.banner.model.entity.Banner;

public record OtherStyleResponse(
        Long id,
        String name,
        String imageUrl
) {

    public static OtherStyleResponse from(Banner banner) {
        return new OtherStyleResponse(
                banner.getId(),
                banner.getBannerTitle(),
                banner.getBannerImageUrl()
        );
    }
}
