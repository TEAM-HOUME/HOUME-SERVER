package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;

import java.util.Optional;

public interface UserRepositoryCustom {
    Long countByMemberIdAndStatus(Long userId);

    Optional<GenerateImage> findImageHistoryById(Long userId);
}
