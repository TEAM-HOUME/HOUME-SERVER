package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanResponse;

public interface AdminFloorPlanService {

    AdminFloorPlanImageUploadResponse createImageUploadUrl(AdminFloorPlanImageUploadRequest request, String contentType);

    AdminFloorPlanResponse create(AdminFloorPlanCreateRequest request);

    AdminFloorPlanListResponse getAll();

    AdminFloorPlanResponse getById(Long floorPlanId);

    AdminFloorPlanResponse update(Long floorPlanId, AdminFloorPlanUpdateRequest request);

    void delete(Long floorPlanId);
}
