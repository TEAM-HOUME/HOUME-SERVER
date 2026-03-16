package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFloorPlanServiceImpl implements AdminFloorPlanService {

    private final FloorPlanRepository floorPlanRepository;
    private final AdminFloorPlanSupport adminFloorPlanSupport;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminFloorPlanImageUploadResponse createImageUploadUrl(AdminFloorPlanImageUploadRequest request, String contentType) {
        return adminFloorPlanSupport.createImageUploadUrl(request, contentType);
    }

    @Override
    @Transactional
    public AdminFloorPlanResponse create(AdminFloorPlanCreateRequest request) {
        List<FloorPlanImageItem> images = adminFloorPlanSupport.normalizeImages(request.images());
        FloorPlanImageItem representativeImage = adminFloorPlanSupport.extractRepresentativeImage(images);

        FloorPlan floorPlan = FloorPlan.create(
                representativeImage.url(),
                representativeImage.filename(),
                representativeImage.originalFilename(),
                representativeImage.fileExtension(),
                request.form(),
                request.structure(),
                request.equilibrium(),
                adminFloorPlanSupport.normalizeRequired(request.floorPlanPrompt()),
                adminFloorPlanSupport.toImagesJson(images)
        );

        return toResponse(floorPlanRepository.saveAndFlush(floorPlan));
    }

    @Override
    public AdminFloorPlanListResponse getAll() {
        List<AdminFloorPlanResponse> floorPlans = floorPlanRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toResponse)
                .toList();
        return new AdminFloorPlanListResponse(floorPlans);
    }

    @Override
    public AdminFloorPlanResponse getById(Long floorPlanId) {
        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
        return toResponse(floorPlan);
    }

    @Override
    @Transactional
    public AdminFloorPlanResponse update(Long floorPlanId, AdminFloorPlanUpdateRequest request) {
        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        List<FloorPlanImageItem> images = request.images() != null
                ? adminFloorPlanSupport.normalizeImages(request.images())
                : adminFloorPlanSupport.resolveImages(floorPlan);
        FloorPlanImageItem representativeImage = adminFloorPlanSupport.extractRepresentativeImage(images);

        floorPlan.update(
                representativeImage.url(),
                representativeImage.filename(),
                representativeImage.originalFilename(),
                representativeImage.fileExtension(),
                request.form() != null ? request.form() : floorPlan.getForm(),
                request.structure() != null ? request.structure() : floorPlan.getStructure(),
                request.equilibrium() != null ? request.equilibrium() : floorPlan.getEquilibrium(),
                request.floorPlanPrompt() != null ? adminFloorPlanSupport.normalizeRequired(request.floorPlanPrompt()) : floorPlan.getFloorPlanPrompt(),
                adminFloorPlanSupport.toImagesJson(images)
        );

        return toResponse(floorPlanRepository.saveAndFlush(floorPlan));
    }

    @Override
    @Transactional
    public void delete(Long floorPlanId) {
        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
        floorPlanRepository.delete(floorPlan);
        floorPlanRepository.flush();
    }

    private AdminFloorPlanResponse toResponse(FloorPlan floorPlan) {
        List<FloorPlanImageItem> images = adminFloorPlanSupport.resolveImages(floorPlan);
        return new AdminFloorPlanResponse(
                floorPlan.getId(),
                floorPlan.getForm(),
                floorPlan.getStructure(),
                floorPlan.getEquilibrium(),
                floorPlan.getFloorPlanPrompt(),
                floorPlan.getUrl(),
                images.stream().map(AdminFloorPlanImageResponse::of).toList()
        );
    }
}
