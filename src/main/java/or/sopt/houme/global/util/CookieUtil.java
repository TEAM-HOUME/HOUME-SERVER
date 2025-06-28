package or.sopt.houme.global.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    /**
     * 지정된 이름, 값, 만료 시간, 보안 설정으로 HTTP 쿠키를 생성하여 반환합니다.
     *
     * @param name 쿠키의 이름
     * @param value 쿠키의 값
     * @param maxAgeSeconds 쿠키의 만료 시간(초 단위)
     * @param secure 쿠키의 Secure 플래그 설정 여부
     * @return 설정된 Cookie 객체
     */
    public static Cookie createSecureCookie(String name, String value, int maxAgeSeconds, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }
}
