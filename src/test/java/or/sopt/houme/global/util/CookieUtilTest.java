package or.sopt.houme.global.util;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CookieUtil Class Test")
class CookieUtilTest {


    @Test
    @DisplayName("createSecureCookie()를 이용해서 Secure 쿠키를 생성 할 수 있다")
    void testCreateSecureCookie() {

        // Given
        String name = "testCookie";
        String value = "testValue";
        int maxAge = 3600;
        boolean secure = true;

        // When
        Cookie cookie = CookieUtil.createSecureCookie(name, value, maxAge, secure);

        // Then
        assertNotNull(cookie);
        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(maxAge, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
    }


    @Test
    @DisplayName("createSecureCookie()를 이용해서 unSecure 쿠키를 생성 할 수 있다")
    void testCreateNonSecureCookie() {
        // Given
        String name = "nonSecureCookie";
        String value = "value";
        int maxAge = 1800;
        boolean secure = false;

        // When
        Cookie cookie = CookieUtil.createSecureCookie(name, value, maxAge, secure);

        // Then
        assertNotNull(cookie);
        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(maxAge, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
    }
}
