package or.sopt.houme.domain.house.service.floorPlan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateDetailItemResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateDetailResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateItemResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.RecentFloorPlanItemResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanEquilibriumJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanFormJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanStructureJsonCodec;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.api.handler.ValidException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FloorPlanServiceImpl implements FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;
    private final GenerateImageRepository generateImageRepository;
    private final FloorPlanImageJsonCodec floorPlanImageJsonCodec;
    private final FloorPlanFormJsonCodec floorPlanFormJsonCodec;
    private final FloorPlanStructureJsonCodec floorPlanStructureJsonCodec;
    private final FloorPlanEquilibriumJsonCodec floorPlanEquilibriumJsonCodec;

    // 집 구조 도면 제공 서비스 (조건에 받아서)
    @Cacheable(
            value = "floorPlanListCache",
            key = "'structure:' + #structure.name()"
    )
    @Override
    public FloorPlanListResponse getHousingPlan(Form form, Structure structure) {

        List<FloorPlan> allByStructureAndType = floorPlanRepository.findAll().stream()
                .filter(floorPlan -> containsForm(floorPlan, form) && containsStructure(floorPlan, structure))
                .toList();

        List<FloorPlanResponse> list = allByStructureAndType.stream()
                .map(FloorPlanResponse::of)
                .toList();

        return new FloorPlanListResponse(list);
    }

    @Override
    public RecentFloorPlanResponse getRecentFloorPlan(User user) {
        if (user == null) {
            return RecentFloorPlanResponse.noRecent();
        }

        Optional<GenerateImage> recentGenerateImage = generateImageRepository.findMostRecentByUserId(user.getId());
        if (recentGenerateImage.isEmpty()) {
            return RecentFloorPlanResponse.noRecent();
        }

        FloorPlan floorPlan = recentGenerateImage.get().getHouse().getHouseFloorPlans().stream()
                .sorted((left, right) -> Long.compare(safeHouseFloorPlanId(left), safeHouseFloorPlanId(right)))
                .map(HouseFloorPlan::getFloorPlan)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (floorPlan == null) {
            return RecentFloorPlanResponse.noRecent();
        }

        FloorPlanImageItem representativeImage = resolveRepresentativeFloorPlanImage(floorPlan);
        return RecentFloorPlanResponse.withRecent(
                RecentFloorPlanItemResponse.of(floorPlan, representativeImage.url(), representativeImage.view())
        );
    }

    @Override
    public ExploreHouseTemplateListResponse getExploreHouseTemplates(
            Integer size,
            Form residenceType,
            Structure layoutType,
            Equilibrium equilibrium,
            User user
    ) {
        validateSize(size);

        List<FloorPlan> allFloorPlans = floorPlanRepository.findAll().stream()
                .sorted((left, right) -> Long.compare(left.getId(), right.getId()))
                .toList();

        List<FloorPlan> exactFloorPlans = allFloorPlans.stream()
                .filter(floorPlan -> matchesExactFilter(floorPlan, residenceType, layoutType, equilibrium))
                .toList();

        boolean isExact = !exactFloorPlans.isEmpty();
        List<FloorPlan> selectedFloorPlans = isExact
                ? exactFloorPlans
                : recommendSimilarFloorPlans(allFloorPlans, residenceType, layoutType);

        List<FloorPlan> limitedFloorPlans = applySize(selectedFloorPlans, size);
        Long latestFloorPlanId = resolveLatestFloorPlanId(user);

        List<ExploreHouseTemplateItemResponse> responses = limitedFloorPlans.stream()
                .map(floorPlan -> {
                    FloorPlanImageItem representative = resolveRepresentativeFloorPlanImage(floorPlan);
                    return ExploreHouseTemplateItemResponse.of(
                            floorPlan,
                            representative.url(),
                            latestFloorPlanId != null && latestFloorPlanId.equals(floorPlan.getId())
                    );
                })
                .toList();

        return ExploreHouseTemplateListResponse.of(isExact, responses);
    }

    @Override
    public ExploreHouseTemplateDetailResponse getExploreHouseTemplateDetail(Long floorPlanId) {
        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        List<FloorPlanImageItem> images;
        try {
            images = floorPlanImageJsonCodec.read(floorPlan.getImagesJson());
        } catch (Exception e) {
            log.warn("Failed to parse floor plan images_json. floorPlanId={}", floorPlan.getId(), e);
            images = fallbackImages(floorPlan);
        }
        if (images.isEmpty()) {
            images = fallbackImages(floorPlan);
        }

        List<ExploreHouseTemplateDetailItemResponse> responses = images.stream()
                .filter(image -> image.url() != null && !image.url().isBlank())
                .map(image -> ExploreHouseTemplateDetailItemResponse.of(image.url(), image.view()))
                .toList();

        return ExploreHouseTemplateDetailResponse.of(
                floorPlan.getId(),
                floorPlan.getFloorPlanName(),
                floorPlan.getEquilibrium() != null ? floorPlan.getEquilibrium().getDescription() : null,
                responses
        );
    }

    private long safeHouseFloorPlanId(HouseFloorPlan mapping) {
        if (mapping == null || mapping.getId() == null) {
            return Long.MAX_VALUE;
        }
        return mapping.getId();
    }

    private FloorPlanImageItem resolveRepresentativeFloorPlanImage(FloorPlan floorPlan) {
        List<FloorPlanImageItem> images;
        try {
            images = floorPlanImageJsonCodec.read(floorPlan.getImagesJson());
        } catch (Exception e) {
            log.warn("Failed to parse floor plan images_json. floorPlanId={}", floorPlan.getId(), e);
            images = fallbackImages(floorPlan);
        }
        if (images.isEmpty()) {
            images = fallbackImages(floorPlan);
        }

        return images.stream()
                .filter(item -> item.url() != null && !item.url().isBlank())
                .findFirst()
                .orElseGet(() -> fallbackImages(floorPlan).getFirst());
    }

    private List<FloorPlanImageItem> fallbackImages(FloorPlan floorPlan) {
        return List.of(FloorPlanImageItem.create(
                floorPlan.getUrl(),
                floorPlan.getFilename(),
                floorPlan.getOriginalFilename(),
                floorPlan.getFileExtension(),
                1,
                null
        ));
    }

    private void validateSize(Integer size) {
        if (size != null && size < 1) {
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }

    private boolean matchesExactFilter(
            FloorPlan floorPlan,
            Form residenceType,
            Structure layoutType,
            Equilibrium equilibrium
    ) {
        if (residenceType != null && !containsForm(floorPlan, residenceType)) {
            return false;
        }
        if (layoutType != null && !containsStructure(floorPlan, layoutType)) {
            return false;
        }
        if (equilibrium != null && !containsEquilibrium(floorPlan, equilibrium)) {
            return false;
        }
        return true;
    }

    private List<FloorPlan> recommendSimilarFloorPlans(
            List<FloorPlan> allFloorPlans,
            Form residenceType,
            Structure layoutType
    ) {
        if (residenceType == null && layoutType == null) {
            return allFloorPlans;
        }

        List<FloorPlan> similar = allFloorPlans.stream()
                .filter(floorPlan ->
                        (residenceType != null && containsForm(floorPlan, residenceType))
                                || (layoutType != null && containsStructure(floorPlan, layoutType)))
                .toList();

        if (!similar.isEmpty()) {
            return deduplicateById(similar);
        }
        return allFloorPlans;
    }

    private List<FloorPlan> applySize(List<FloorPlan> floorPlans, Integer size) {
        if (size == null) {
            return floorPlans;
        }
        return floorPlans.stream().limit(size).toList();
    }

    private Long resolveLatestFloorPlanId(User user) {
        if (user == null) {
            return null;
        }

        return generateImageRepository.findMostRecentByUserId(user.getId())
                .map(GenerateImage::getHouse)
                .flatMap(house -> house.getHouseFloorPlans().stream()
                        .sorted((left, right) -> Long.compare(safeHouseFloorPlanId(left), safeHouseFloorPlanId(right)))
                        .map(HouseFloorPlan::getFloorPlan)
                        .filter(java.util.Objects::nonNull)
                .map(FloorPlan::getId)
                .findFirst())
                .orElse(null);
    }

    private boolean containsForm(FloorPlan floorPlan, Form form) {
        return form == null || resolveForms(floorPlan).contains(form);
    }

    private boolean containsStructure(FloorPlan floorPlan, Structure structure) {
        return structure == null || resolveStructures(floorPlan).contains(structure);
    }

    private boolean containsEquilibrium(FloorPlan floorPlan, Equilibrium equilibrium) {
        return equilibrium == null || resolveEquilibriums(floorPlan).contains(equilibrium);
    }

    // 기존 단일 컬럼 데이터도 계속 조회되도록 JSON 값이 없으면 대표 enum 컬럼으로 fallback 한다.
    private List<Form> resolveForms(FloorPlan floorPlan) {
        List<Form> forms = floorPlanFormJsonCodec.read(floorPlan.getFormsJson());
        if (!forms.isEmpty()) {
            return forms;
        }
        return floorPlan.getForm() == null ? List.of() : List.of(floorPlan.getForm());
    }

    private List<Structure> resolveStructures(FloorPlan floorPlan) {
        List<Structure> structures = floorPlanStructureJsonCodec.read(floorPlan.getStructuresJson());
        if (!structures.isEmpty()) {
            return structures;
        }
        return floorPlan.getStructure() == null ? List.of() : List.of(floorPlan.getStructure());
    }

    private List<Equilibrium> resolveEquilibriums(FloorPlan floorPlan) {
        List<Equilibrium> equilibriums = floorPlanEquilibriumJsonCodec.read(floorPlan.getEquilibriumsJson());
        if (!equilibriums.isEmpty()) {
            return equilibriums;
        }
        return floorPlan.getEquilibrium() == null ? List.of() : List.of(floorPlan.getEquilibrium());
    }

    private List<FloorPlan> deduplicateById(List<FloorPlan> floorPlans) {
        Map<Long, FloorPlan> byId = floorPlans.stream()
                .collect(Collectors.toMap(
                        FloorPlan::getId,
                        floorPlan -> floorPlan,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return List.copyOf(byId.values());
    }
}
