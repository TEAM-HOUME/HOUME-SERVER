package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;

import java.util.List;

public interface UserRepositoryCustom {
    long countByMemberIdAndStatus(Long userId);

    List<UserImageHistoryDTO> getUserImageHistory(Long userId);
}
