package or.sopt.houme.domain.carousel.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.carousel.entity.Carousel;
import or.sopt.houme.domain.carousel.repository.CarouselRepository;
import or.sopt.houme.domain.preference.entity.CarouselPreference;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.repository.CarouselPreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarouselServiceImpl implements CarouselService {

    private final CarouselRepository carouselRepository;
    private final PreferenceRepository preferenceRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;


    @Override
    public GetCarouselListResponseDTO getCarousel(int page) {

        // pageable 객체를 만들어서 한 번에 세 개의 레코드를 조회하도록 설정
        Pageable pageable = PageRequest.of(page, 5);

        List<GetCarouselResponseDTO> list = carouselRepository.findAll(pageable)
                .stream()
                .map(GetCarouselResponseDTO::from)
                .toList();

        return GetCarouselListResponseDTO.of(list);
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
