package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingResponse;

public interface AdminLandingService {

    AdminBannerImageUploadResponse createImageUploadUrl(AdminBannerImageUploadRequest request, String contentType);

    AdminLandingResponse create(AdminLandingCreateRequest request);

    AdminLandingListResponse getAll();

    AdminLandingResponse getById(Long landingId);

    AdminLandingResponse update(Long landingId, AdminLandingUpdateRequest request);

    void delete(Long landingId);
}
