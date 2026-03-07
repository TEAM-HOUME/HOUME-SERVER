package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.model.entity.NicknameWord;
import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import or.sopt.houme.domain.user.repository.NicknameWordRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NicknameServiceTest {

    @InjectMocks
    private NicknameService nicknameService;

    @Mock
    private NicknameWordRepository nicknameWordRepository;

    @Test
    @DisplayName("닉네임 rotate 결과는 한글 단어 + 한글 단어 + 숫자 4자리 형식이다")
    void rotateNickname_format() {
        given(nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.ADJECTIVE))
                .willReturn(List.of(NicknameWord.of(NicknameWordType.ADJECTIVE, "느긋한")));
        given(nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.NOUN))
                .willReturn(List.of(NicknameWord.of(NicknameWordType.NOUN, "펭귄")));

        String nickname = nicknameService.rotateNickname();

        assertThat(nickname).matches("^[가-힣]+[가-힣]+\\d{4}$");
    }

    @Test
    @DisplayName("활성화된 닉네임 리소스가 없으면 정해진 예외가 발생한다")
    void rotateNickname_whenWordsMissing_throwException() {
        given(nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.ADJECTIVE))
                .willReturn(List.of());
        given(nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.NOUN))
                .willReturn(List.of(NicknameWord.of(NicknameWordType.NOUN, "펭귄")));

        UserException exception = assertThrows(UserException.class, () -> nicknameService.rotateNickname());

        assertEquals(ErrorCode.NICKNAME_RESOURCE_EMPTY, exception.getErrorCode());
    }
}
