package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.house.model.carousel.entity.Carousel;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselServiceImpl implements CarouselService {

    private final CarouselRepository carouselRepository;
    private final PreferenceRepository preferenceRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;
    private final CarouselCacheService carouselCacheService;

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



    /**
     * 캐러셀 좋아요를 저장하는 메서드 입니다
     *
     * carouselPreference 를 탐색하여 회원과 캐러셀이 존재하는지 확인하고
     * 존재한다면 like 인지 탐색하고 최종적으로 like 상태일 수 있도록 저장합니다
     *
     * 존재하지 않는다면 like 상태의 엔티티를 새롭게 생성합니다
     * */
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
