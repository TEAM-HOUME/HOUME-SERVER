package or.sopt.houme.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NicknameServiceTest {

    private final NicknameService nicknameService = new NicknameService();

    @Test
    @DisplayName("닉네임 rotate 결과는 한글 단어 + 한글 단어 + 숫자 4자리 형식이다")
    void rotateNickname_format() {
        String nickname = nicknameService.rotateNickname();

        assertThat(nickname).matches("^[가-힣]+[가-힣]+\\d{4}$");
    }
}
