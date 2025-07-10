package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
}
