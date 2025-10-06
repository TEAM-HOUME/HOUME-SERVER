package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.GenerateImagePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenerateImagePreferenceRepository extends JpaRepository<GenerateImagePreference, Long> {

    // 이미지에 대한 선호도 최신순으로 가져오기
    Optional<GenerateImagePreference> findFirstByGenerateImageIdOrderByIdDesc(Long generateImageId);
}
