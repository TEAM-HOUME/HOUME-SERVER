
package or.sopt.houme.domain.openai.client;

import or.sopt.houme.domain.openai.controller.dto.ChatGptImageRequest;
import or.sopt.houme.domain.openai.controller.dto.ChatGptImageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "openaiImageClient",
        url = "https://api.openai.com/v1/images"
)
public interface OpenAIImageClient {
    @PostMapping(value = "/generations", consumes = "application/json")
    ChatGptImageResponse generateImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChatGptImageRequest request
    );
}
