package or.sopt.houme.domain.factor.service;

import or.sopt.houme.domain.preference.dto.response.FactorsResponse;
import or.sopt.houme.domain.preference.entity.Factor;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.entity.PreferenceFactor;
import or.sopt.houme.domain.preference.repository.FactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceFactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.preference.service.FactorServiceImpl;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PreferenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FactorServiceImplTest {
    @Mock
    private FactorRepository factorRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private PreferenceFactorRepository preferenceFactorRepository;

    @InjectMocks
    private FactorServiceImpl factorService;

    @Test
    @DisplayName("좋아요 여부로 factors 조회 성공")
    void getFactors_success() {
        // given
        Factor factor1 = Factor.builder().id(1L).isLike(true).build();
        Factor factor2 = Factor.builder().id(2L).isLike(true).build();
        when(factorRepository.findFactorsByIsLike(true)).thenReturn(List.of(factor1, factor2));

        // when
        FactorsResponse response = factorService.getFactors(true);

        // then
        assertThat(response.factors()).hasSize(2);
        assertThat(response.factors()).extracting("id").containsExactly(1L, 2L);
        verify(factorRepository, times(1)).findFactorsByIsLike(true);
    }

    @Test
    @DisplayName("toggleFactorLog - preference 없음 -> 예외 발생")
    void toggleFactorLog_notFoundPreference() {
        // given
        User user = User.builder().id(1L).build();
        when(preferenceRepository.findPreferenceByUserIdAndImageId(1L, 100L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> factorService.toggleFactorLog(user, 100L, 10L))
                .isInstanceOf(PreferenceException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_PREFERENCE.getMsg());
    }

    @Test
    @DisplayName("toggleFactorLog - factor 없음 -> 예외 발생")
    void toggleFactorLog_notFoundFactor() {
        // given
        Long factorId = 10L;
        User user = User.builder().id(1L).build();
        Preference preference = Preference.builder().id(1L).isLike(true).build();
        when(preferenceRepository.findPreferenceByUserIdAndImageId(1L, 100L)).thenReturn(Optional.of(preference));
        when(factorRepository.findById(factorId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> factorService.toggleFactorLog(user, 100L, factorId))
                .isInstanceOf(PreferenceException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_FACTOR.getMsg());
    }

    @Test
    @DisplayName("toggleFactorLog - isLike 불일치 -> 예외 발생")
    void toggleFactorLog_mismatchedIsLike() {
        // given
        Long factorId = 10L;
        User user = User.builder().id(1L).build();
        Preference preference = Preference.builder().id(1L).isLike(true).build();
        Factor factor = Factor.builder().id(factorId).isLike(false).build();

        when(preferenceRepository.findPreferenceByUserIdAndImageId(1L, 100L)).thenReturn(Optional.of(preference));
        when(factorRepository.findById(factorId)).thenReturn(Optional.of(factor));

        // when & then
        assertThatThrownBy(() -> factorService.toggleFactorLog(user, 100L, factorId))
                .isInstanceOf(PreferenceException.class)
                .hasMessageContaining(ErrorCode.MISMATCHED_IS_LIKE.getMsg());
    }

    @Test
    @DisplayName("toggleFactorLog - 이미 존재하면 삭제")
    void toggleFactorLog_deleteExisting() {
        // given
        Long factorId = 10L;
        User user = User.builder().id(1L).build();
        Preference preference = Preference.builder().id(1L).isLike(true).build();
        Factor factor = Factor.builder().id(factorId).isLike(true).build();
        PreferenceFactor existing = PreferenceFactor.of(preference, factor);

        when(preferenceRepository.findPreferenceByUserIdAndImageId(1L, 100L)).thenReturn(Optional.of(preference));
        when(factorRepository.findById(factorId)).thenReturn(Optional.of(factor));
        when(preferenceFactorRepository.findByPreference(preference)).thenReturn(Optional.of(existing));

        // when
        factorService.toggleFactorLog(user, 100L, factorId);

        // then
        verify(preferenceFactorRepository, times(1)).delete(existing);
    }

    @Test
    @DisplayName("toggleFactorLog - 존재하지 않으면 저장")
    void toggleFactorLog_saveNew() {
        // given
        Long factorId = 10L;
        User user = User.builder().id(1L).build();
        Preference preference = Preference.builder().id(1L).isLike(true).build();
        Factor factor = Factor.builder().id(factorId).isLike(true).build();

        when(preferenceRepository.findPreferenceByUserIdAndImageId(1L, 100L)).thenReturn(Optional.of(preference));
        when(factorRepository.findById(factorId)).thenReturn(Optional.of(factor));
        when(preferenceFactorRepository.findByPreference(preference)).thenReturn(Optional.empty());

        // when
        factorService.toggleFactorLog(user, 100L, factorId);

        // then
        verify(preferenceFactorRepository, times(1)).save(any(PreferenceFactor.class));
    }
}
