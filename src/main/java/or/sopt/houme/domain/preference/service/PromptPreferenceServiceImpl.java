package or.sopt.houme.domain.preference.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.PromptPreference;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.preference.repository.PromptPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromptPreferenceServiceImpl implements PromptPreferenceService {

    private final PromptPreferenceRepository promptPreferenceRepository;
    private final PreferenceRepository preferenceRepository;

    // 좋아요 or 싫어요
    @Transactional
    @Override
    public void togglePromptPreference(House house, boolean isLike) {
        // house.id로 기존 선호도 데이터를 조회
        Optional<PromptPreference> promptPreferenceOptional =
                promptPreferenceRepository.findPreferenceByHouseId(house.getId());

        if (promptPreferenceOptional.isPresent()) {
            // 이미 좋아요/싫어요 상태가 있는 경우
            PromptPreference promptPreference = promptPreferenceOptional.get();
            Preference preference = promptPreference.getPreference();

            // 기존 상태와 요청 상태가 다를 경우에만 업데이트
            if (preference.isLike() != isLike) {
                preference.updateLike(isLike);
            }

        } else {
            // 좋아요 상태가 없고, '좋아요' 또는 '싫어요' 요청이 들어온 경우
            // 새로운 Preference와 PromptPreference를 생성
            Preference newPreference = Preference.of(isLike);
            preferenceRepository.save(newPreference);

            PromptPreference newPromptPreference = PromptPreference.builder()
                    .house(house)
                    .preference(newPreference)
                    .build();
            promptPreferenceRepository.save(newPromptPreference);
        }
    }

}
