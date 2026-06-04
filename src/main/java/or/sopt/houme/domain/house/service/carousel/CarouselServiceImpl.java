package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.model.carousel.entity.Carousel;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselV2ListResponseDTO;
import or.sopt.houme.domain.house.repository.HouseFurnitureRepository;
import or.sopt.houme.domain.house.repository.carousel.CarouselRepository;
import or.sopt.houme.domain.preference.model.entity.CarouselPreference;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.repository.CarouselPreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselServiceImpl implements CarouselService {
    private static final int CAROUSEL_V2_SIZE = 10;
    private static final int PRIORITY_FURNITURE_MATCH_SIZE = 4;
    private static final int RANDOM_SEGMENT_COUNT = 3;

    private final CarouselCacheService carouselCacheService;
    private final CarouselRepository carouselRepository;
    private final PreferenceRepository preferenceRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final JjymService jjymService;
    private final CarouselLikeLogService carouselLikeLogService;
    private final GenerateImageRepository generateImageRepository;
    private final HouseFurnitureRepository houseFurnitureRepository;

    @Override
    public GetCarouselListResponseDTO getCarousel(int page) {
        int pageSize = 5;
        List<GetCarouselResponseDTO> shuffled = carouselCacheService.getShuffledCarouselList(); // 고정된 셔플 결과

        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, shuffled.size());
        if (fromIndex >= shuffled.size()) {
            return GetCarouselListResponseDTO.of(List.of());
        }

        List<GetCarouselResponseDTO> result = shuffled.subList(fromIndex, toIndex).stream()
                .toList();

        return GetCarouselListResponseDTO.of(result);
    }

    @Override
    public GetCarouselV2ListResponseDTO getCarouselV2(Long cursor, User user) {
        Long userId = user.getId();
        if (cursor == null) {
            List<CurationRawProduct> initialSelected = getInitialCarouselV2Selection(userId);
            List<GetCarouselResponseDTO> initialResult = initialSelected.stream()
                    .map(rawProduct -> GetCarouselResponseDTO.of(rawProduct.getId(), rawProduct.getProductImageUrl()))
                    .toList();
            Long nextCursor = initialSelected.size() < CAROUSEL_V2_SIZE
                    ? null
                    : initialSelected.get(initialSelected.size() - 1).getId();
            return GetCarouselV2ListResponseDTO.of(initialResult, nextCursor);
        }

        List<CurationRawProduct> selected = new ArrayList<>();

        selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserWithCursor(
                userId,
                cursor,
                CAROUSEL_V2_SIZE,
                List.of()
        ));

        List<GetCarouselResponseDTO> result = selected.stream()
                .map(rawProduct -> GetCarouselResponseDTO.of(rawProduct.getId(), rawProduct.getProductImageUrl()))
                .toList();
        Long nextCursor = selected.size() < CAROUSEL_V2_SIZE
                ? null
                : selected.get(selected.size() - 1).getId();

        return GetCarouselV2ListResponseDTO.of(result, nextCursor);
    }

    @Override
    @Transactional
    public void likeCarousel(User user, Long carouselId) {
        updateLike(user.getId(), carouselId, true);
    }

    @Override
    @Transactional
    public void hateCarousel(User user, Long carouselId) {
        updateLike(user.getId(), carouselId, false);
    }

    @Transactional
    public void likeCarouselV2WithLog(User user, Long rawProductId) {
        jjymService.likeRawProduct(user.getId(), rawProductId);
        carouselLikeLogService.createLikeLog(user, rawProductId);
    }

    private List<CurationRawProduct> getInitialCarouselV2Selection(Long userId) {
        Long maxId = curationRawProductRepository.findMaxExposedRawProductIdExcludingLikedByUser(userId);
        if (maxId == null) {
            return List.of();
        }

        List<CurationRawProduct> selected = new ArrayList<>();
        addPreferredFurnitureProducts(userId, selected);
        addFurnitureCategoryProducts(userId, selected);
        addDiversifiedRandomProducts(userId, maxId, selected);

        return selected.size() > CAROUSEL_V2_SIZE
                ? new ArrayList<>(selected.subList(0, CAROUSEL_V2_SIZE))
                : selected;
    }

    private void addPreferredFurnitureProducts(Long userId, List<CurationRawProduct> selected) {
        if (selected.size() >= CAROUSEL_V2_SIZE) {
            return;
        }

        List<Long> preferredFurnitureIds = resolveRecentSelectedFurnitureIds(userId);
        if (preferredFurnitureIds.isEmpty()) {
            return;
        }

        selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByFurnitureIds(
                userId,
                preferredFurnitureIds,
                SoozipCategory.FURNITURE,
                Math.min(PRIORITY_FURNITURE_MATCH_SIZE, CAROUSEL_V2_SIZE - selected.size()),
                extractRawProductIds(selected)
        ));
    }

    private void addFurnitureCategoryProducts(Long userId, List<CurationRawProduct> selected) {
        if (selected.size() >= CAROUSEL_V2_SIZE) {
            return;
        }

        selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                userId,
                SoozipCategory.FURNITURE,
                CAROUSEL_V2_SIZE - selected.size(),
                extractRawProductIds(selected)
        ));
    }

    private void addDiversifiedRandomProducts(Long userId, Long maxId, List<CurationRawProduct> selected) {
        if (selected.size() >= CAROUSEL_V2_SIZE) {
            return;
        }

        List<Long> anchors = resolveRandomStartIds(userId, maxId);
        for (int index = 0; index < anchors.size() && selected.size() < CAROUSEL_V2_SIZE; index++) {
            int remaining = CAROUSEL_V2_SIZE - selected.size();
            int perSegmentSize = Math.max(1, (int) Math.ceil((double) remaining / (anchors.size() - index)));
            selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserWithCursor(
                    userId,
                    anchors.get(index) + 1L,
                    perSegmentSize,
                    extractRawProductIds(selected)
            ));
        }

        if (selected.size() < CAROUSEL_V2_SIZE) {
            selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserWithCursor(
                    userId,
                    null,
                    CAROUSEL_V2_SIZE - selected.size(),
                    extractRawProductIds(selected)
            ));
        }
    }

    private List<Long> resolveRecentSelectedFurnitureIds(Long userId) {
        Optional<GenerateImage> recentGenerateImage = generateImageRepository.findMostRecentByUserId(userId);
        if (recentGenerateImage.isEmpty() || recentGenerateImage.get().getHouse() == null) {
            return List.of();
        }

        Long houseId = recentGenerateImage.get().getHouse().getId();
        if (houseId == null) {
            return List.of();
        }

        return houseFurnitureRepository.findAllByHouseIdWithFurniture(houseId).stream()
                .map(HouseFurniture::getFurniture)
                .filter(java.util.Objects::nonNull)
                .map(furniture -> furniture.getId())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> resolveRandomStartIds(Long userId, Long maxId) {
        if (maxId == null || maxId < 1L) {
            return List.of();
        }

        long firstAnchor = resolveRandomStartId(userId, maxId);
        long segmentSpan = Math.max(1L, maxId / RANDOM_SEGMENT_COUNT);
        Set<Long> anchors = new LinkedHashSet<>();
        anchors.add(firstAnchor);
        for (int index = 1; index < RANDOM_SEGMENT_COUNT; index++) {
            anchors.add(((firstAnchor + (segmentSpan * index) - 1L) % maxId) + 1L);
        }
        return anchors.stream().toList();
    }

    private List<Long> extractRawProductIds(List<CurationRawProduct> selected) {
        return selected.stream()
                .map(CurationRawProduct::getId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private long resolveRandomStartId(Long userId, Long maxId) {
        long seed = java.time.LocalDate.now().toEpochDay() ^ userId;
        long positive = Math.abs(seed == Long.MIN_VALUE ? 0L : seed);
        return (positive % maxId) + 1L;
    }

    private void updateLike(Long userId, Long carouselId, boolean isLike) {
        Carousel carousel = findCarousel(carouselId);

        Optional<CarouselPreference> optional = carouselPreferenceRepository.findByUserIdAndCarouselId(userId, carouselId);

        if (optional.isPresent()) {
            Preference preference = optional.get().getPreference();
            if (preference.isLike() != isLike) {
                preference.updateLike(isLike);
            }
        } else {
            Preference preference = Preference.of(isLike);
            preferenceRepository.save(preference);
            preferenceRepository.flush();

            CarouselPreference carouselPreference = CarouselPreference.of(preference, carousel, userId);
            carouselPreferenceRepository.save(carouselPreference);
        }
    }

    private Carousel findCarousel(Long carouselId) {
        return carouselRepository.findById(carouselId)
                .orElseThrow(() -> new CarouselException(ErrorCode.CAROUSEL_NOT_FOUND));
    }

}
