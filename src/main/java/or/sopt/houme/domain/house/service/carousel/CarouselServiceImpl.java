package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.house.model.carousel.entity.Carousel;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselV2ListResponseDTO;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselServiceImpl implements CarouselService {
    private static final int CAROUSEL_V2_SIZE = 10;

    private final CarouselCacheService carouselCacheService;
    private final CarouselRepository carouselRepository;
    private final PreferenceRepository preferenceRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final JjymService jjymService;
    private final CarouselLikeLogService carouselLikeLogService;

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
        List<CurationRawProduct> selected = new ArrayList<>();

        Long effectiveCursor = cursor;
        if (effectiveCursor == null) {
            Long maxId = curationRawProductRepository.findMaxExposedRawProductIdExcludingLikedByUser(userId);
            if (maxId == null) {
                return GetCarouselV2ListResponseDTO.of(List.of(), null);
            }
            long randomStartId = resolveRandomStartId(userId, maxId);
            effectiveCursor = randomStartId + 1L;
        }

        selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserWithCursor(
                userId,
                effectiveCursor,
                CAROUSEL_V2_SIZE,
                List.of()
        ));

        if (cursor == null && selected.size() < CAROUSEL_V2_SIZE) {
            List<Long> excludedIds = selected.stream().map(CurationRawProduct::getId).toList();
            selected.addAll(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserWithCursor(
                    userId,
                    null,
                    CAROUSEL_V2_SIZE - selected.size(),
                    excludedIds
            ));
        }

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
