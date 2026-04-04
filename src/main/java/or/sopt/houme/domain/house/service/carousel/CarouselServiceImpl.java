package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselServiceImpl implements CarouselService {

    private final CarouselCacheService carouselCacheService;
    private final CurationRawProductRepository curationRawProductRepository;
    private final JjymService jjymService;

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
    public GetCarouselListResponseDTO getCarouselV2(int page, User user) {
        int pageSize = 5;
        List<GetCarouselResponseDTO> result = findExposedRawProductsExcludingJjym(user.getId(), page, pageSize)
                .stream()
                .map(rawProduct -> GetCarouselResponseDTO.of(rawProduct.getId(), rawProduct.getProductImageUrl()))
                .toList();

        return GetCarouselListResponseDTO.of(result);
    }
    @Override
    @Transactional
    public void likeCarousel(User user, Long carouselId) {
        jjymService.likeRawProduct(user.getId(), carouselId);
    }

    @Override
    @Transactional
    public void hateCarousel(User user, Long carouselId) {
        // hate 는 찜 해제와 분리한다.
    }

    private Page<CurationRawProduct> findExposedRawProductsExcludingJjym(
            Long userId,
            int page,
            int pageSize
    ) {
        List<Long> likedProductIds = jjymService.getLikedRawProductProductIds(userId);
        if (likedProductIds.isEmpty()) {
            return curationRawProductRepository.findAllByIsExposedTrueOrderByIdDesc(PageRequest.of(page, pageSize));
        }
        return curationRawProductRepository.findAllByIsExposedTrueAndProductIdNotInOrderByIdDesc(
                likedProductIds,
                PageRequest.of(page, pageSize)
        );
    }

}
