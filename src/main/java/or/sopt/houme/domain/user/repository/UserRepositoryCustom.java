package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.generatedImage.entity.GenerateImage;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    long countByMemberIdAndStatus(Long userId);

    List<UserImageHistoryDTO> getUserImageHistory(Long userId);

    Optional<GenerateImage> findImageHistoryById(Long userId);
}
