package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImages;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageRequest;
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
    private final AdminFloorPlanImageUploadService adminFloorPlanImageUploadService;
    private final FloorPlanImageJsonCodec floorPlanImageJsonCodec;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminFloorPlanImageUploadResponse createImageUploadUrl(AdminFloorPlanImageUploadRequest request, String contentType) {
        return adminFloorPlanImageUploadService.createImageUploadUrl(request, contentType);
    }

    @Override
    @Transactional
    public AdminFloorPlanResponse create(AdminFloorPlanCreateRequest request) {
        FloorPlanImages images = toFloorPlanImages(request.images());

        FloorPlan floorPlan = FloorPlan.create(
                request.form(),
                request.structure(),
                request.equilibrium(),
                normalizeRequired(request.floorPlanPrompt()),
                images,
                floorPlanImageJsonCodec.write(images.items())
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

        FloorPlanImages images = request.images() != null
                ? toFloorPlanImages(request.images())
                : resolveImages(floorPlan);

        floorPlan.update(
                request.form() != null ? request.form() : floorPlan.getForm(),
                request.structure() != null ? request.structure() : floorPlan.getStructure(),
                request.equilibrium() != null ? request.equilibrium() : floorPlan.getEquilibrium(),
                request.floorPlanPrompt() != null ? normalizeRequired(request.floorPlanPrompt()) : floorPlan.getFloorPlanPrompt(),
                images,
                floorPlanImageJsonCodec.write(images.items())
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
        FloorPlanImages images = resolveImages(floorPlan);
        return new AdminFloorPlanResponse(
                floorPlan.getId(),
                floorPlan.getForm(),
                floorPlan.getStructure(),
                floorPlan.getEquilibrium(),
                floorPlan.getFloorPlanPrompt(),
                floorPlan.getUrl(),
                images.items().stream().map(AdminFloorPlanImageResponse::of).toList()
        );
    }

    private FloorPlanImages toFloorPlanImages(List<AdminFloorPlanImageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        List<FloorPlanImageItem> items = requests.stream()
                .map(this::toFloorPlanImageItem)
                .toList();
        return FloorPlanImages.from(items);
    }

    private FloorPlanImages resolveImages(FloorPlan floorPlan) {
        List<FloorPlanImageItem> parsedImages = floorPlanImageJsonCodec.read(floorPlan.getImagesJson());
        FloorPlanImageItem legacyImage = null;
        if (floorPlan.getUrl() != null && !floorPlan.getUrl().isBlank()) {
            legacyImage = FloorPlanImageItem.create(
                    floorPlan.getUrl(),
                    floorPlan.getFilename(),
                    floorPlan.getOriginalFilename(),
                    floorPlan.getFileExtension(),
                    1
            );
        }
        return FloorPlanImages.restore(parsedImages, legacyImage);
    }

    private FloorPlanImageItem toFloorPlanImageItem(AdminFloorPlanImageRequest request) {
        if (request == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return FloorPlanImageItem.create(
                request.url(),
                request.filename(),
                request.originalFilename(),
                request.fileExtension(),
                request.sortOrder()
        );
    }

    private String normalizeRequired(String value) {
        if (value == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return trimmed;
    }
}
