package or.sopt.houme.domain.carousel.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.carousel.entity.Carousel;
import or.sopt.houme.domain.carousel.entity.CarouselType;
import or.sopt.houme.domain.carousel.repository.CarouselRepository;
import or.sopt.houme.domain.preference.entity.CarouselPreference;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.repository.CarouselPreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarouselServiceImpl implements CarouselService {

    private final CarouselRepository carouselRepository;
    private final PreferenceRepository preferenceRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;

    @Cacheable(value = "getCarouselListCache", key = "'page:' + #page")
    @Override
    public GetCarouselListResponseDTO getCarousel(int page) {
        int pageSize = 5;

        // 1. 모든 캐러셀 불러오기
        List<Carousel> allCarousels = carouselRepository.findAll();

        // 2. 타입별로 그룹화
        Map<CarouselType, Queue<Carousel>> grouped = allCarousels.stream()
                .collect(Collectors.groupingBy(
                        Carousel::getCarouselType,
                        Collectors.toCollection(LinkedList::new)
                ));

        // 3. 라운드로빈 방식으로 타입 섞기
        List<Carousel> shuffled = new ArrayList<>();
        while (!grouped.values().stream().allMatch(Queue::isEmpty)) {
            for (Queue<Carousel> queue : grouped.values()) {
                if (!queue.isEmpty()) {
                    shuffled.add(queue.poll());
                }
            }
        }

        // 4. 페이지네이션 적용
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, shuffled.size());
        if (fromIndex >= shuffled.size()) {
            return GetCarouselListResponseDTO.of(List.of());
        }

        List<GetCarouselResponseDTO> result = shuffled.subList(fromIndex, toIndex).stream()
                .map(GetCarouselResponseDTO::from)
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
