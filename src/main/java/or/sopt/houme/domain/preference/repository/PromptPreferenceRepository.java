package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.PromptPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PromptPreferenceRepository extends JpaRepository<PromptPreference, Long> {

    @Query("SELECT pp FROM PromptPreference pp WHERE pp.house.id = :houseId ORDER BY pp.id DESC")
    Optional<PromptPreference> findTopByHouseIdOrderByIdDesc(Long houseId);
}
