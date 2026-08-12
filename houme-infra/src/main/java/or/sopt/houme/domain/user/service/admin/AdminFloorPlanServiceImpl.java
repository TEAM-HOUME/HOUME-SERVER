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
import or.sopt.houme.domain.user.util.floorplan.FloorPlanEquilibriumJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanFormJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImagePresignedUrlGenerator;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanStructureJsonCodec;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
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
    private final FloorPlanImagePresignedUrlGenerator floorPlanImagePresignedUrlGenerator;
    private final FloorPlanImageJsonCodec floorPlanImageJsonCodec;
    private final FloorPlanFormJsonCodec floorPlanFormJsonCodec;
    private final FloorPlanStructureJsonCodec floorPlanStructureJsonCodec;
    private final FloorPlanEquilibriumJsonCodec floorPlanEquilibriumJsonCodec;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminFloorPlanImageUploadResponse createImageUploadUrl(AdminFloorPlanImageUploadRequest request, String contentType) {
        return floorPlanImagePresignedUrlGenerator.createImageUploadUrl(request, contentType);
    }

    @Override
    @Transactional
    public AdminFloorPlanResponse create(AdminFloorPlanCreateRequest request) {
        FloorPlanImages images = toFloorPlanImages(request.images());
        List<Form> forms = deduplicateRequired(request.forms());
        List<Structure> structures = deduplicateRequired(request.structures());
        List<Equilibrium> equilibriums = deduplicateRequired(request.equilibriums());

        FloorPlan floorPlan = FloorPlan.create(
                normalizeRequired(request.name()),
                forms,
                structures,
                equilibriums,
                normalizeRequired(request.floorPlanPrompt()),
                images,
                floorPlanImageJsonCodec.write(images.items()),
                floorPlanFormJsonCodec.write(forms),
                floorPlanStructureJsonCodec.write(structures),
                floorPlanEquilibriumJsonCodec.write(equilibriums)
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
        List<Form> forms = request.forms() != null
                ? deduplicateRequired(request.forms())
                : resolveForms(floorPlan);
        List<Structure> structures = request.structures() != null
                ? deduplicateRequired(request.structures())
                : resolveStructures(floorPlan);
        List<Equilibrium> equilibriums = request.equilibriums() != null
                ? deduplicateRequired(request.equilibriums())
                : resolveEquilibriums(floorPlan);

        floorPlan.update(
                request.name() != null ? normalizeRequired(request.name()) : floorPlan.getFloorPlanName(),
                forms,
                structures,
                equilibriums,
                request.floorPlanPrompt() != null ? normalizeRequired(request.floorPlanPrompt()) : floorPlan.getFloorPlanPrompt(),
                images,
                floorPlanImageJsonCodec.write(images.items()),
                floorPlanFormJsonCodec.write(forms),
                floorPlanStructureJsonCodec.write(structures),
                floorPlanEquilibriumJsonCodec.write(equilibriums)
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
        // 기존 단일 컬럼은 대표값으로 유지하고, admin 응답은 JSON 기반 다중 매핑을 기준으로 복원한다.
        return new AdminFloorPlanResponse(
                floorPlan.getId(),
                floorPlan.getFloorPlanName(),
                resolveForms(floorPlan),
                resolveStructures(floorPlan),
                resolveEquilibriums(floorPlan),
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
                    1,
                    null
            );
        }
        return FloorPlanImages.restore(parsedImages, legacyImage);
    }

    private List<Form> resolveForms(FloorPlan floorPlan) {
        List<Form> forms = floorPlanFormJsonCodec.read(floorPlan.getFormsJson());
        if (!forms.isEmpty()) {
            return forms;
        }
        return floorPlan.getForm() != null ? List.of(floorPlan.getForm()) : List.of();
    }

    private List<Structure> resolveStructures(FloorPlan floorPlan) {
        List<Structure> structures = floorPlanStructureJsonCodec.read(floorPlan.getStructuresJson());
        if (!structures.isEmpty()) {
            return structures;
        }
        return floorPlan.getStructure() != null ? List.of(floorPlan.getStructure()) : List.of();
    }

    private List<Equilibrium> resolveEquilibriums(FloorPlan floorPlan) {
        List<Equilibrium> equilibriums = floorPlanEquilibriumJsonCodec.read(floorPlan.getEquilibriumsJson());
        if (!equilibriums.isEmpty()) {
            return equilibriums;
        }
        return floorPlan.getEquilibrium() != null ? List.of(floorPlan.getEquilibrium()) : List.of();
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
                request.sortOrder(),
                normalizeNullable(request.view())
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

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private <T> List<T> deduplicateRequired(List<T> values) {
        if (values == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        List<T> normalized = values.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return normalized;
    }
}
