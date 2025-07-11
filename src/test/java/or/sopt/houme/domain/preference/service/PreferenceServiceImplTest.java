package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[Preference Service] Test")
class PreferenceServiceImplTest {

    @Autowired
    PreferenceServiceImpl preferenceService;

    @Autowired
    PreferenceRepository preferenceRepository;

    @BeforeEach
    void setUp() {
        preferenceRepository.deleteAll();
    }
    @Test
    @DisplayName("좋아요 객체를 저장 할 수 있다.")
    void createPreference() {
        // Given
        boolean isTrue = true;

        // When
        Preference save = preferenceService.createPreference(isTrue);

        // Then
        assertThat(save).isNotNull();
        assertThat(save.getId()).isEqualTo(1L);
        assertThat(save.isLike()).isTrue();
    }
}