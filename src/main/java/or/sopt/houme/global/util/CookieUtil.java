package or.sopt.houme.global.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    public static Cookie createSecureCookie(String name, String value, int maxAgeSeconds, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }
}
