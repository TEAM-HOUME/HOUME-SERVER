package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.model.entity.GenerateImagePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface GenerateImagePreferenceRepository extends JpaRepository<GenerateImagePreference, Long> {

    // 이미지에 대한 선호도 최신순으로 가져오기
    Optional<GenerateImagePreference> findFirstByGenerateImageIdOrderByIdDesc(Long generateImageId);

    // 이미지에 대한 모든 선호도 가져오기 (과거 데이터 정리 목적)
    List<GenerateImagePreference> findAllByGenerateImageId(Long generateImageId);

    long countByGenerateImageId(Long generateImageId);
}
