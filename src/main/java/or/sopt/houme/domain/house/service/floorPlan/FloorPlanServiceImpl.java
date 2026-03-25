package or.sopt.houme.domain.house.service.floorPlan;

import lombok.RequiredArgsConstructor;
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
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
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
public class FloorPlanServiceImpl implements FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;
    private final GenerateImageRepository generateImageRepository;
    private final FloorPlanImageJsonCodec floorPlanImageJsonCodec;

    // 집 구조 도면 제공 서비스 (조건에 받아서)
    @Cacheable(
            value = "floorPlanListCache",
            key = "'structure:' + #structure.name()"
    )
    @Override
    public FloorPlanListResponse getHousingPlan(Form form, Structure structure) {

        List<FloorPlan> allByStructureAndType =
                floorPlanRepository.findAllByStructure(structure);

        List<FloorPlanResponse> list = allByStructureAndType.stream()
                .map(FloorPlanResponse::of)
                .toList();

        return new FloorPlanListResponse(list);
    }

    @Override
    public RecentFloorPlanResponse getRecentFloorPlan(User user) {
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

        List<FloorPlanImageItem> images = floorPlanImageJsonCodec.read(floorPlan.getImagesJson());
        if (images.isEmpty()) {
            images = List.of(FloorPlanImageItem.create(
                    floorPlan.getUrl(),
                    floorPlan.getFilename(),
                    floorPlan.getOriginalFilename(),
                    floorPlan.getFileExtension(),
                    1,
                    null
            ));
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
        return floorPlanImageJsonCodec.read(floorPlan.getImagesJson()).stream()
                .filter(item -> item.url() != null && !item.url().isBlank())
                .findFirst()
                .orElseGet(() -> FloorPlanImageItem.create(
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
        if (residenceType != null && floorPlan.getForm() != residenceType) {
            return false;
        }
        if (layoutType != null && floorPlan.getStructure() != layoutType) {
            return false;
        }
        if (equilibrium != null && floorPlan.getEquilibrium() != equilibrium) {
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
                        (residenceType != null && floorPlan.getForm() == residenceType)
                                || (layoutType != null && floorPlan.getStructure() == layoutType))
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
