package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.entity.PromptPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromptPreferenceRepository extends JpaRepository<PromptPreference, Long> {

    Optional<PromptPreference> findFirstByHouseIdOrderByIdDesc(Long houseId);

    @Query("SELECT p FROM PromptPreference p WHERE p.house.id = :houseId")
    Optional<PromptPreference> findPreferenceByHouseId(@Param("houseId") Long houseId);
}
