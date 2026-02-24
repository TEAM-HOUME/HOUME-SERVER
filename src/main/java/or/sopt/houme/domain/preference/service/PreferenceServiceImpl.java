package or.sopt.houme.domain.preference.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceRepository preferenceRepository;

    // Preference 생성
    @Transactional
    @Override
    public Preference createPreference(boolean isLike) {
        Preference preference = Preference.of(isLike);

        return preferenceRepository.save(preference);
    }

    // Preference 삭제
    @Transactional
    @Override
    public void deletePreference(Long preferenceId) {
        preferenceRepository.deleteById(preferenceId);
    }
}
