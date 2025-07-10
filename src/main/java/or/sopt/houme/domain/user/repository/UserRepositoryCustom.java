package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    Long countByMemberIdAndStatus(Long userId);

    List<UserImageHistoryDTO> getUserImageHistory(Long userId);

    Optional<GenerateImage> findImageHistoryById(Long userId);
}
