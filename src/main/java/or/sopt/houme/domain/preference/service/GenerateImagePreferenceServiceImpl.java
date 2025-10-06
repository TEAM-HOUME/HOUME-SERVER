package or.sopt.houme.domain.preference.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.preference.entity.GenerateImagePreference;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.repository.GenerateImagePreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceFactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PreferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerateImagePreferenceServiceImpl implements GenerateImagePreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final GenerateImagePreferenceRepository generateImagePreferenceRepository;
    private final PreferenceFactorRepository preferenceFactorRepository;

    // 좋아요 or 싫어요
    @Transactional
    @Override
    public void toggleGenerateImagePreference(GenerateImage generateImage, boolean isLike) {
        // generateImage.id로 기존 선호도 데이터를 조회
        Optional<GenerateImagePreference> generateImagePreferenceOptional =
                generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage.getId());

        if (generateImagePreferenceOptional.isPresent()) {
            // 이미 좋아요/싫어요 상태가 있는 경우
            Preference preference = generateImagePreferenceOptional.get().getPreference();

            // 기존 상태와 요청 상태가 다를 경우에만 업데이트
            if (preference.isLike() != isLike) {
                preferenceFactorRepository.deleteByPreference(preference);
                preference.updateLike(isLike);
            }

        } else {
            // 좋아요 상태가 없고, '좋아요' 또는 '싫어요' 요청이 들어온 경우
            // 새로운 Preference와 PromptPreference를 생성
            Preference newPreference = Preference.of(isLike);
            preferenceRepository.save(newPreference);

            GenerateImagePreference newPromptPreference =
                    GenerateImagePreference.generatePreference(newPreference, generateImage);
            generateImagePreferenceRepository.save(newPromptPreference);
        }
    }

    // 이미지 선호도 삭제 (요인도 삭제됨)
    @Transactional
    @Override
    public Long deleteGenerateImagePreference(GenerateImage generateImage) {
        // 생성된 이미지 선호도 조회
        GenerateImagePreference generateImagePreference = generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage.getId())
                .orElseThrow(() -> new PreferenceException(ErrorCode.NOT_FOUND_PREFERENCE));

        // preferenceId 추출
        Preference preference = generateImagePreference.getPreference();

        // 이미지 선호도 삭제
        generateImagePreferenceRepository.delete(generateImagePreference);

        return preference.getId();
    }
}
