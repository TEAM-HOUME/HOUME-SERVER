package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;

import java.util.List;
import java.util.Optional;

public interface GenerateImageRepositoryCustom {

    Optional<GenerateImage> findByHouseId(Long houseId);

    // 가장 최근 생성된 GenerateImage 1개 가져오기
    Optional<GenerateImage> findLastGenerateImage(Long houseId);

    Optional<GenerateImage> findMostRecentByUserId(Long userId);

    List<GenerateImage> findGenerateImagesByHouseId(Long houseId);

    List<GenerateImage> findAllByUserIdWithHouseAndBanner(Long userId);

    List<GenerateImage> findRelatedImagesByRawProductIds(
            List<Long> rawProductIds,
            Long excludeImageId,
            int limit,
            GenerateImageType generationType
    );
}
