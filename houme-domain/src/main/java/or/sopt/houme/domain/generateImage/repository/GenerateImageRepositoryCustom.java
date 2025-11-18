package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;

import java.util.List;
import java.util.Optional;

public interface GenerateImageRepositoryCustom {

    Optional<GenerateImage> findByHouseId(Long houseId);

    // 가장 최근 생성된 GenerateImage 1개 가져오기
    Optional<GenerateImage> findLastGenerateImage(Long houseId);

    List<GenerateImage> findGenerateImagesByHouseId(Long houseId);
}
