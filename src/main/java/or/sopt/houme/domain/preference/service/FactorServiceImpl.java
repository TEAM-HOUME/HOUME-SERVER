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
        List<Factor> factors = factorRepository.findFactorsByIsLike(isLike);

        return FactorsResponse.from(factors);
    }

    @Override
    @Transactional
    public void toggleFactorLog(User user, Long imageId, Long factorId) {
        // 1. user + imageId로 preference 조회
        Preference preference = preferenceRepository.findPreferenceByUserIdAndImageId(user.getId(), imageId)
                .orElseThrow(() -> new PreferenceException(ErrorCode.NOT_FOUND_PREFERENCE));

        // 2. factor 조회
        Factor factor = factorRepository.findById(factorId)
                .orElseThrow(() -> new PreferenceException(ErrorCode.NOT_FOUND_FACTOR));

        // 3. preferenceFactor 존재 여부 확인
        Optional<PreferenceFactor> existing = preferenceFactorRepository.findByPreferenceAndFactor(preference, factor);

        if (existing.isPresent()) {
            // 이미 있으면 삭제 (toggle off)
            preferenceFactorRepository.delete(existing.get());
        } else {
            // 없으면 새로 저장 (toggle on)
            PreferenceFactor preferenceFactor = PreferenceFactor.of(preference, factor);
            preferenceFactorRepository.save(preferenceFactor);
        }
    }
}
