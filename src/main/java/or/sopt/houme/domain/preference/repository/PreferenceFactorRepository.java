package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Factor;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.PreferenceFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenceFactorRepository extends JpaRepository<PreferenceFactor, Long> {
    Optional<PreferenceFactor> findByPreferenceAndFactor(Preference preference, Factor factor);

    // Preference로 PreferenceFactor 조회
    Optional<PreferenceFactor> findByPreference(Preference preference);
}
