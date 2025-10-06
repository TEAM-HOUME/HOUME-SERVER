package or.sopt.houme.domain.preference.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.preference.dto.response.FactorsResponse;
import or.sopt.houme.domain.preference.entity.Factor;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.PreferenceFactor;
import or.sopt.houme.domain.preference.repository.FactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceFactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PreferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactorServiceImpl implements FactorService {

    private final FactorRepository factorRepository;
    private final PreferenceFactorRepository preferenceFactorRepository;
    private final PreferenceRepository preferenceRepository;

    @Override
    public FactorsResponse getFactors(boolean isLike) {
        // 선호 여부에 따른 요인 리스트 조회
        List<Factor> factors = factorRepository.findFactorsByIsLike(isLike);

        return FactorsResponse.from(factors);
    }

    @Override
    @Transactional
    public void toggleFactorLog(User user, Long imageId, Long factorId) {
        // user + imageId로 preference 조회
        // 유저와 이미지간 존재하는 preference 객체는 유일합니다.
        Preference preference = preferenceRepository.findPreferenceByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new PreferenceException(ErrorCode.NOT_FOUND_PREFERENCE));

        // factor 조회
        Factor factor = factorRepository.findById(factorId)
                .orElseThrow(() -> new PreferenceException(ErrorCode.NOT_FOUND_FACTOR));

        // 선호 여부가 미스매치는 아닌지
        if (!factor.isLike() == preference.isLike()) {
            throw new PreferenceException(ErrorCode.MISMATCHED_IS_LIKE);
        }

        // preferenceFactor 존재 여부 확인
        Optional<PreferenceFactor> byPreference = preferenceFactorRepository.findByPreference(preference);

        // 매핑 객체를 토글링합니다.
        if (byPreference.isPresent()) {
            PreferenceFactor preferenceFactor = byPreference.get();

            // 이미 같은값이 있으면 삭제 (toggle off)
            if (preferenceFactor.getFactor().getId().equals(factor.getId())) {
                preferenceFactorRepository.delete(preferenceFactor);
            } else {
                // 다른 값이라면 업데이트
                preferenceFactor.updateFactor(factor);
            }
        } else {
            // 없으면 새로 저장 (toggle on)
            PreferenceFactor preferenceFactor = PreferenceFactor.of(preference, factor);
            preferenceFactorRepository.save(preferenceFactor);
        }
    }

    // preferenceId로 PreferenceFactor 삭제
    @Transactional
    @Override
    public void deletePreferenceFactor(Long preferenceId) {

        factorRepository.findPreferenceFactorAndDeleteByPreferenceId(preferenceId);
    }
}
