package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.model.entity.PromptPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PromptPreferenceRepository extends JpaRepository<PromptPreference, Long> {

    Optional<PromptPreference> findFirstByHouseIdOrderByIdDesc(Long houseId);

    List<PromptPreference> findAllByHouseId(Long houseId);
}
