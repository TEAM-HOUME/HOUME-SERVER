package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleResponse;

public interface AdminStyleService {

    AdminBannerImageUploadResponse createImageUploadUrl(AdminBannerImageUploadRequest request, String contentType);

    AdminStyleResponse create(AdminStyleCreateRequest request);

    AdminStyleListResponse getAll();

    AdminStyleResponse getById(Long styleId);

    AdminStyleResponse update(Long styleId, AdminStyleUpdateRequest request);

    void delete(Long styleId);

    AdminBannerRawProductSearchResponse searchRawProducts(String keyword, int size);
}
