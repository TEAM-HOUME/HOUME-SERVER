package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;

public interface AdminBannerService {
    AdminBannerResponse create(AdminBannerCreateRequest request);

    AdminBannerListResponse getAll();

    AdminBannerResponse getById(Long bannerId);

    AdminBannerResponse update(Long bannerId, AdminBannerUpdateRequest request);

    void delete(Long bannerId);

    AdminBannerRawProductSearchResponse searchRawProducts(String keyword, int size);
}
