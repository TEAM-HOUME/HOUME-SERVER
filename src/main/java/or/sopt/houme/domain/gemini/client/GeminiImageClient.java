package or.sopt.houme.domain.gemini.client;

import or.sopt.houme.domain.gemini.dto.GeminiImageRequest;
import or.sopt.houme.domain.gemini.dto.GeminiImageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "geminiImageClient",
        url = "${gemini.api-base-url:https://generativelanguage.googleapis.com/v1beta}"
)
public interface GeminiImageClient {

    @PostMapping(
            value = "/models/{model}:generateContent",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    GeminiImageResponse generateImage(
            @PathVariable("model") String model,
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiImageRequest request
    );
}
