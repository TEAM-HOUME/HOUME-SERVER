package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.PromptPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromptPreferenceRepository extends JpaRepository<PromptPreference, Long> {

    Optional<PromptPreference> findFirstByHouseIdOrderByIdDesc(Long houseId);

}
