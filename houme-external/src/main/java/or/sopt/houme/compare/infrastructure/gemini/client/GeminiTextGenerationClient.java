package or.sopt.houme.compare.infrastructure.gemini.client;

import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiTextGenerationRequest;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiTextGenerationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "geminiTextGenerationClient",
        url = "${gemini.api-base-url:https://generativelanguage.googleapis.com/v1beta}"
)
public interface GeminiTextGenerationClient {

    @PostMapping(
            value = "/models/{model}:generateContent",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    GeminiTextGenerationResponse generateContent(
            @PathVariable("model") String model,
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiTextGenerationRequest request
    );
}
