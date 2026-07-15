package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.user.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    Long countByMemberIdAndStatus(Long userId);

    Optional<GenerateImage> findImageHistoryById(Long userId);

    // 이메일 완전일치 또는 닉네임 부분일치로 회원 검색 (id 오름차순, limit 제한)
    List<User> searchMembers(String keyword, int limit);
}
