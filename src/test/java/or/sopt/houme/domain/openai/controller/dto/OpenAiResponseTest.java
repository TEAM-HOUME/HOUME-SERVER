package or.sopt.houme.domain.openai.controller.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class OpenAiResponseTest {


    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    @DisplayName("JSON → OpenAiResponse 역직렬화가 정상 작동 할 수 있다")
    void testDeserialization() throws Exception {
        String json = """
                {
                    "data": [
                        {
                            "url": "https://example.com/image.png",
                            "revised_prompt": "A beautiful sunset",
                            "b64_json": "iVBORw0KGgoAAAANSUhEUgAAA..."
                        }
                    ]
                }
                """;

        OpenAiResponse response = objectMapper.readValue(json, OpenAiResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);

        OpenAiResponse.ImageData imageData = response.getData().get(0);
        assertThat(imageData.getUrl()).isEqualTo("https://example.com/image.png");
        assertThat(imageData.getRevised_prompt()).isEqualTo("A beautiful sunset");
        assertThat(imageData.getB64_json()).startsWith("iVBOR");
    }


    @Test
    @DisplayName("OpenAiResponse → JSON 직렬화가 정상 작동 할 수 있다")
    void testSerialization() throws Exception {
        OpenAiResponse.ImageData imageData = new OpenAiResponse.ImageData(
                "https://example.com/image.png",
                "A peaceful mountain",
                "abc123base64encoded"
        );

        OpenAiResponse response = new OpenAiResponse(List.of(imageData));

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("https://example.com/image.png");
        assertThat(json).contains("A peaceful mountain");
        assertThat(json).contains("abc123base64encoded");
    }


    @Test
    @DisplayName("기본 생성자와 setter를 통해 객체 생성이 가능하다")
    void testDefaultConstructorAndSetters() {
        OpenAiResponse.ImageData imageData = new OpenAiResponse.ImageData();
        imageData.setUrl("https://test.com");
        imageData.setRevised_prompt("test prompt");
        imageData.setB64_json("base64test");

        assertThat(imageData.getUrl()).isEqualTo("https://test.com");
        assertThat(imageData.getRevised_prompt()).isEqualTo("test prompt");
        assertThat(imageData.getB64_json()).isEqualTo("base64test");
    }


    @Test
    @DisplayName("ImageData equals, hashCode, toString 메서드 테스트")
    void testEqualsHashCodeToString() {
        // given
        OpenAiResponse.ImageData image1 = new OpenAiResponse.ImageData(
                "https://example.com", "prompt", "base64"
        );
        OpenAiResponse.ImageData image2 = new OpenAiResponse.ImageData(
                "https://example.com", "prompt", "base64"
        );
        OpenAiResponse.ImageData image3 = new OpenAiResponse.ImageData(
                "https://another.com", "different", "zzz"
        );

        // equals
        assertEquals(image1, image2);
        assertNotEquals(image1, image3);

        // hashCode
        assertEquals(image1.hashCode(), image2.hashCode());
        assertNotEquals(image1.hashCode(), image3.hashCode());

        // toString (간단히 null 아님만 확인)
        assertNotNull(image1.toString());
        assertTrue(image1.toString().contains("url"));
        assertTrue(image1.toString().contains("prompt"));
    }


    @Test
    @DisplayName("canEqual(Object) 테스트")
    void testCanEqual() {
        OpenAiResponse.ImageData imageData = new OpenAiResponse.ImageData();
        assertTrue(imageData.canEqual(new OpenAiResponse.ImageData()));
        assertFalse(imageData.canEqual("not an ImageData"));
    }


    @Test
    @DisplayName("OpenAiResponse equals, hashCode, toString 테스트")
    void testEqualsHashCodeToString_success() {
        // given
        OpenAiResponse.ImageData image = new OpenAiResponse.ImageData("url", "prompt", "b64");
        OpenAiResponse response1 = new OpenAiResponse(List.of(image));
        OpenAiResponse response2 = new OpenAiResponse(List.of(image));
        OpenAiResponse response3 = new OpenAiResponse(List.of(
                new OpenAiResponse.ImageData("other", "different", "xxx")
        ));

        // when & then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);

        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());

        assertNotNull(response1.toString());
        assertTrue(response1.toString().contains("data"));
    }


    @Test
    @DisplayName("OpenAiResponse canEqual(Object) 테스트")
    void testCanEqual_success() {
        OpenAiResponse response = new OpenAiResponse();
        assertTrue(response.canEqual(new OpenAiResponse()));
        assertFalse(response.canEqual("not a response"));
    }
}
