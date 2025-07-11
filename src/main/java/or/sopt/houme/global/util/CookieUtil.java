package or.sopt.houme.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    public static Cookie createSecureCookie(String name, String value, int maxAgeSeconds, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    public static void addSameSiteCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds, boolean secure) {
        String cookieValue = String.format(
                "%s=%s; Path=/; Max-Age=%d; Domain=.houme.kr; HttpOnly%s; SameSite=None",
                name,
                value,
                maxAgeSeconds,
                secure ? "; Secure" : ""
        );

        response.addHeader("Set-Cookie", cookieValue);
    }


}
