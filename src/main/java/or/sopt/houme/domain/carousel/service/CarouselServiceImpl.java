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
    public void likeCarousel(User user, Long carouselId){

        Carousel findCarousel = findCarousel(carouselId);

        // 회원과 캐러셀에 해당하는 선호도 레코드가 존재하는지 확인
        boolean exists = carouselPreferenceRepository.existsByUserIdAndCarouselId(user.getId(), carouselId);

        if(exists){

            // 존재한다면 객체를 가져와서 좋아요를 업데이트 함
            CarouselPreference carouselPreference = carouselPreferenceRepository
                    .findByUserIdAndCarouselId(user.getId(), carouselId)
                    .orElseThrow(() -> new CarouselException(ErrorCode.CAROUSEL_PREFERENCE_NOT_FOUND));

            Preference preference = carouselPreference.getPreference();

            if (!preference.isLike()) {
                preference.updateLike(true);
            }

            return;
        }

        // 존재하지 않는다면 새로운 객체를 생성함
        Preference preference = Preference.of(true);
        CarouselPreference carouselPreference = CarouselPreference.of(preference,findCarousel,user.getId());
        preferenceRepository.save(preference);
        carouselPreferenceRepository.save(carouselPreference);
    }


    @Override
    @Transactional
    public void hateCarousel(User user, Long carouselId) {
        Carousel findCarousel = findCarousel(carouselId);
        boolean exists = carouselPreferenceRepository.existsByUserIdAndCarouselId(user.getId(), carouselId);

        if (exists) {
            CarouselPreference carouselPreference = carouselPreferenceRepository
                    .findByUserIdAndCarouselId(user.getId(), carouselId)
                    .orElseThrow(() -> new CarouselException(ErrorCode.CAROUSEL_PREFERENCE_NOT_FOUND));

            Preference preference = carouselPreference.getPreference();

            if (preference.isLike()) {
                preference.updateLike(false);
            }

            return;
        }

        // 존재하지 않는다면 새로운 '싫어요' 선호도 객체를 생성
        Preference preference = Preference.of(false);
        CarouselPreference carouselPreference = CarouselPreference.of(preference, findCarousel, user.getId());

        preferenceRepository.save(preference);
        carouselPreferenceRepository.save(carouselPreference);
    }




    private Carousel findCarousel(Long carouselId) {
        return carouselRepository.findById(carouselId)
                .orElseThrow(() -> new CarouselException(ErrorCode.CAROUSEL_NOT_FOUND));
    }
}
