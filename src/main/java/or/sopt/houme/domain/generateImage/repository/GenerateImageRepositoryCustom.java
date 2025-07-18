package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;

import java.util.Optional;

public interface GenerateImageRepositoryCustom {
    Optional<GenerateImage> findGenerateImageByUserIdAndImageId(Long userId, Long imageId);

    Optional<GenerateImage> findByHouseId(Long houseId);
}
