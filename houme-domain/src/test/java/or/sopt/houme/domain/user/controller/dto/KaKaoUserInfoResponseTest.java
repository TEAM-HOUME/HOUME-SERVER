package or.sopt.houme.domain.user.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KaKaoUserInfoResponseTest {

    @Test
    @DisplayName("KaKaoUserInfoResponse 및 하위 클래스의 getter/setter 테스트")
    void testDtoAccessors() {
        // given
        KaKaoUserInfoResponse.Properties properties = new KaKaoUserInfoResponse.Properties();
        properties.setNickname("홍길동");

        KaKaoUserInfoResponse.KakaoAccount.Profile profile = new KaKaoUserInfoResponse.KakaoAccount.Profile();
        profile.setNickname("프로필닉");
        profile.setIs_default_nickname(true);

        KaKaoUserInfoResponse.KakaoAccount kakaoAccount = new KaKaoUserInfoResponse.KakaoAccount();
        kakaoAccount.setEmail("test@kakao.com");
        kakaoAccount.setHas_email(true);
        kakaoAccount.setIs_email_verified(true);
        kakaoAccount.setIs_email_valid(true);
        kakaoAccount.setEmail_needs_agreement(false);
        kakaoAccount.setProfile_nickname_needs_agreement(false);
        kakaoAccount.setProfile(profile);

        KaKaoUserInfoResponse response = new KaKaoUserInfoResponse();
        response.setId(12345L);
        response.setConnected_at("2025-07-11T20:00:00Z");
        response.setProperties(properties);
        response.setKakao_account(kakaoAccount);

        // when & then
        assertEquals(12345L, response.getId());
        assertEquals("2025-07-11T20:00:00Z", response.getConnected_at());
        assertEquals("홍길동", response.getProperties().getNickname());

        assertEquals("test@kakao.com", response.getKakao_account().getEmail());
        assertTrue(response.getKakao_account().getHas_email());
        assertTrue(response.getKakao_account().getIs_email_verified());
        assertTrue(response.getKakao_account().getIs_email_valid());
        assertFalse(response.getKakao_account().getEmail_needs_agreement());
        assertFalse(response.getKakao_account().getProfile_nickname_needs_agreement());
        assertEquals("프로필닉", response.getKakao_account().getProfile().getNickname());
        assertTrue(response.getKakao_account().getProfile().getIs_default_nickname());
    }
}
