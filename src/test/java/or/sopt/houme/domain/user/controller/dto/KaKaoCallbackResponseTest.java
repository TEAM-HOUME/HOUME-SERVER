package or.sopt.houme.domain.user.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KaKaoCallbackResponseTest {

    @Test
    @DisplayName("KaKaoCallbackResponse getter/setter, equals, hashCode, toString 테스트")
    void testDto() {
        // Given
        KaKaoCallbackResponse response1 = new KaKaoCallbackResponse();
        response1.setCode("authCode");
        response1.setError("errorType");
        response1.setError_description("에러 설명");
        response1.setState("xyz");

        KaKaoCallbackResponse response2 = new KaKaoCallbackResponse(
                "authCode", "errorType", "에러 설명", "xyz"
        );

        // When & Then
        assertEquals("authCode", response1.getCode());
        assertEquals("errorType", response1.getError());
        assertEquals("에러 설명", response1.getError_description());
        assertEquals("xyz", response1.getState());

        // equals & hashCode는 명시적으로 정의하지 않았으므로 Object 기준 비교
        assertNotEquals(response1, response2); // equals 오버라이드 안 했으므로 주소 비교
        assertNotEquals(response1.hashCode(), response2.hashCode());

        // toString
        assertNotNull(response1.toString());
    }
}
