package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response;

import java.util.List;

public record AdminBannerListResponse(
        List<AdminBannerResponse> banners
) {
}
