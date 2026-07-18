package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.carousel.domain.Carousel;
import or.sopt.houme.carousel.domain.port.out.CarouselRepositoryPort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselCacheService {

    private final CarouselRepositoryPort carouselRepository;

    @Cacheable(value = "getCarouselListCache", key = "'shuffled'")
    public List<GetCarouselResponseDTO> getShuffledCarouselList() {
        List<Carousel> allCarousels = carouselRepository.findAll();

        // 타입별로 그룹화 + 라운드로빈 셔플 (타입은 id 기준으로 그룹화)
        Map<Long, Queue<Carousel>> grouped = allCarousels.stream()
                .collect(Collectors.groupingBy(
                        Carousel::getCarouselTypeId,
                        Collectors.toCollection(LinkedList::new)
                ));

        List<GetCarouselResponseDTO> shuffled = new ArrayList<>();
        while (!grouped.values().stream().allMatch(Queue::isEmpty)) {
            for (Queue<Carousel> queue : grouped.values()) {
                if (!queue.isEmpty()) {
                    shuffled.add(GetCarouselResponseDTO.from(queue.poll()));
                }
            }
        }

        return shuffled;
    }

}
