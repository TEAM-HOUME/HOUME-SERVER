package or.sopt.houme.domain.carousel.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.carousel.repository.CarouselRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarouselServiceImpl implements CarouselService {

    private final CarouselRepository carouselRepository;

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
}
