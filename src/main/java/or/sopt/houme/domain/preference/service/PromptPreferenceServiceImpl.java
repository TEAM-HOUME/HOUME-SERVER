package or.sopt.houme.domain.preference.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.PromptPreference;
import or.sopt.houme.domain.preference.repository.PromptPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromptPreferenceServiceImpl implements PromptPreferenceService {

    private final PromptPreferenceRepository promptPreferenceRepository;

    // 집 프롬프트 좋아요 생성
    @Transactional
    @Override
    public void createPromptPreference(House house, Preference preference) {
        PromptPreference promptPreference = PromptPreference.builder()
                .house(house)
                .preference(preference)
                .build();

        promptPreferenceRepository.save(promptPreference);
    }

}
