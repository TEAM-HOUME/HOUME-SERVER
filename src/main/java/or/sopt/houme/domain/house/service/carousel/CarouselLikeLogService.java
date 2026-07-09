package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.house.model.carousel.entity.CarouselLikeLog;
import or.sopt.houme.domain.house.model.carousel.entity.CarouselLikeLogAction;
import or.sopt.houme.domain.house.repository.carousel.CarouselLikeLogRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.FurnitureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CarouselLikeLogService {

    private final CarouselLikeLogRepository carouselLikeLogRepository;
    private final CurationRawProductRepository curationRawProductRepository;

    public void createLikeLog(User user, Long rawProductId) {
        carouselLikeLogRepository.save(CarouselLikeLog.of(user, findRawProduct(rawProductId), CarouselLikeLogAction.LIKE));
    }

    public void createHateLog(User user, Long rawProductId) {
        carouselLikeLogRepository.save(CarouselLikeLog.of(user, findRawProduct(rawProductId), CarouselLikeLogAction.HATE));
    }

    private CurationRawProduct findRawProduct(Long rawProductId) {
        return curationRawProductRepository.findById(rawProductId)
                .orElseThrow(() -> new FurnitureException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));
    }
}
