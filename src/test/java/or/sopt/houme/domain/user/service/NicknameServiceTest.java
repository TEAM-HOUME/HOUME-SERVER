package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import or.sopt.houme.user.domain.port.out.NicknameWordPort;
import or.sopt.houme.user.domain.port.out.UserRepositoryPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NicknameServiceTest {

    @InjectMocks
    private NicknameService nicknameService;

    @Mock
    private NicknameWordPort nicknameWordPort;

    @Mock
    private UserRepositoryPort userRepository;

    @Test
    @DisplayName("닉네임 rotate 결과는 등록된 단어 조합 형식을 유지한다")
    void rotateNickname_format() {
        List<String> adjectives = List.of(
                "느긋한",
                "반짝이는"
        );
        List<String> nouns = List.of(
                "펭귄",
                "고양이"
        );

        given(nicknameWordPort.findActiveWordsByType(NicknameWordType.ADJECTIVE))
                .willReturn(adjectives);
        given(nicknameWordPort.findActiveWordsByType(NicknameWordType.NOUN))
                .willReturn(nouns);

        Set<String> allowedWords = Set.of(
                "느긋한펭귄",
                "느긋한고양이",
                "반짝이는펭귄",
                "반짝이는고양이"
        );

        for (int i = 0; i < 50; i++) {
            String nickname = nicknameService.rotateNickname();

            assertThat(nickname).matches("^[가-힣]+[가-힣]+$");
            assertThat(allowedWords).contains(nickname);
        }
    }

    @Test
    @DisplayName("닉네임 태그 생성 결과는 #과 숫자 4자리 형식을 유지한다")
    void generateNicknameTag_format() {
        given(userRepository.existsByNicknameAndNicknameTag(org.mockito.ArgumentMatchers.eq("느긋한펭귄"), org.mockito.ArgumentMatchers.anyString()))
                .willReturn(false);

        String nicknameTag = nicknameService.generateNicknameTag("느긋한펭귄");

        assertThat(nicknameTag).matches("^#\\d{4}$");
    }

    @Test
    @DisplayName("닉네임 태그 생성이 반복 충돌하면 예외가 발생한다")
    void generateNicknameTag_whenCollisionsRepeat_throwException() {
        given(userRepository.existsByNicknameAndNicknameTag(org.mockito.ArgumentMatchers.eq("느긋한펭귄"), org.mockito.ArgumentMatchers.anyString()))
                .willReturn(true);

        UserException exception = assertThrows(UserException.class, () -> nicknameService.generateNicknameTag("느긋한펭귄"));

        assertEquals(ErrorCode.NICKNAME_TAG_GENERATION_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("닉네임이 비어 있으면 유효성 예외가 발생한다")
    void generateNicknameTag_whenNicknameBlank_throwException() {
        UserException exception = assertThrows(UserException.class,
                () -> nicknameService.generateNicknameTag("   "));

        assertEquals(ErrorCode.NOT_VALID_EXCEPTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("활성화된 닉네임 리소스가 없으면 정해진 예외가 발생한다")
    void rotateNickname_whenWordsMissing_throwException() {
        given(nicknameWordPort.findActiveWordsByType(NicknameWordType.ADJECTIVE))
                .willReturn(List.of());
        given(nicknameWordPort.findActiveWordsByType(NicknameWordType.NOUN))
                .willReturn(List.of("펭귄"));

        UserException exception = assertThrows(UserException.class, () -> nicknameService.rotateNickname());

        assertEquals(ErrorCode.NICKNAME_RESOURCE_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("활성화된 명사 리소스가 없으면 정해진 예외가 발생한다")
    void rotateNickname_whenNounsMissing_throwException() {
        given(nicknameWordPort.findActiveWordsByType(NicknameWordType.ADJECTIVE))
                .willReturn(List.of("느긋한"));
        given(nicknameWordPort.findActiveWordsByType(NicknameWordType.NOUN))
                .willReturn(List.of());

        UserException exception = assertThrows(UserException.class, () -> nicknameService.rotateNickname());

        assertEquals(ErrorCode.NICKNAME_RESOURCE_EMPTY, exception.getErrorCode());
    }
}
