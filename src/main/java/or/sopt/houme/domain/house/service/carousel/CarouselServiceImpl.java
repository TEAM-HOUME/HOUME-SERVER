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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselServiceImpl implements CarouselService {
    private final CarouselCacheService carouselCacheService;
    private final CarouselRepository carouselRepository;
    private final PreferenceRepository preferenceRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final JjymService jjymService;
    private final CarouselLikeLogService carouselLikeLogService;
    private final CarouselCandidateService carouselCandidateService;
    private final CarouselShuffleService carouselShuffleService;

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
    public GetCarouselV2ListResponseDTO getCarouselV2(User user, List<Long> furnitureIds) {
        var candidateBundle = carouselCandidateService.collectCandidates(user.getId(), furnitureIds);
        List<Long> displayIds = carouselShuffleService.selectDisplayIds(candidateBundle, user.getId());
        if (displayIds.isEmpty()) {
            return GetCarouselV2ListResponseDTO.of(List.of());
        }

        Map<Long, CurationRawProduct> rawProductById = curationRawProductRepository.findAllById(displayIds).stream()
                .collect(Collectors.toMap(CurationRawProduct::getId, Function.identity()));

        List<GetCarouselResponseDTO> result = displayIds.stream()
                .map(rawProductById::get)
                .filter(java.util.Objects::nonNull)
                .map(rawProduct -> GetCarouselResponseDTO.of(rawProduct.getId(), rawProduct.getProductImageUrl()))
                .toList();

        return GetCarouselV2ListResponseDTO.of(result);
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
