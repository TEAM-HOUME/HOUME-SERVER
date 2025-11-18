package or.sopt.houme.global.util;

import io.micrometer.common.lang.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class CookieUtil {

    public static Cookie createSecureCookie(String name, String value, int maxAgeSeconds, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

/*    public static void addSameSiteCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds, boolean secure) {
        String cookieValue = String.format(
                "%s=%s; Path=/; Max-Age=%d; Domain=.houme.kr; HttpOnly%s; SameSite=None",
                name,
                value,
                maxAgeSeconds,
                secure ? "; Secure" : ""
        );

        response.addHeader("Set-Cookie", cookieValue);
    }

    *//**
     * HttpOnly + SameSite=None + Secure 설정을 유지한 채
     * 해당 이름의 쿠키를 즉시 만료시키는 헬퍼 메서드
     *
     * @param response  HttpServletResponse
     * @param name      쿠키 이름
     * @param secure    Secure 플래그 (로컬 http 개발환경이면 false, 배포환경은 true)
     *//*
    public static void deleteCookie(HttpServletResponse response, String name, boolean secure) {
        String expiredCookie = String.format(
                "%s=; Path=/; Max-Age=0; Domain=.houme.kr; HttpOnly%s; SameSite=None",
                name,
                secure ? "; Secure" : ""
        );

        response.addHeader("Set-Cookie", expiredCookie);
    }*/


    public static void addSameSiteCookie(HttpServletResponse response,
                                 String name, String value,
                                 int maxAgeSeconds,
                                 String domain,
                                 boolean secure,
                                 String sameSite) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly",
                name, value, maxAgeSeconds));

        if (StringUtils.hasText(domain)) {
            sb.append("; Domain=").append(domain);
        }
        if (secure) {                       // prod ⇒ true, local ⇒ false
            sb.append("; Secure");
        }
        if (StringUtils.hasText(sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }

        response.addHeader("Set-Cookie", sb.toString());
    }



    public static void deleteCookie(HttpServletResponse response,
                                    String name,
                                    @Nullable String domain,
                                    boolean secure,
                                    @Nullable String sameSite) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s=; Path=/; Max-Age=0; HttpOnly", name));   // 값 비우고 즉시 만료

        // Domain
        if (StringUtils.hasText(domain)) {
            sb.append("; Domain=").append(domain);
        }

        // Secure
        if (secure) {                       // prod ⇒ true, local ⇒ false
            sb.append("; Secure");
        }

        // SameSite
        if (StringUtils.hasText(sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }

        response.addHeader("Set-Cookie", sb.toString());
    }


}
