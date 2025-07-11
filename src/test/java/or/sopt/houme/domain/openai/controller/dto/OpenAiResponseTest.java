package or.sopt.houme.domain.openai.controller.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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
}
