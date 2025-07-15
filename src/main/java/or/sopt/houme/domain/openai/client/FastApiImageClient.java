/*
package or.sopt.houme.domain.openai.client;

import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "imageClient",
        url = "${external.image-api.base-url}"  // ex: http://localhost:8000
)
public interface FastApiImageClient {

    @PostMapping(
        value = "/images",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE  // StreamingResponse
    )
    byte[] generateImage(@RequestBody PromptRequestDTO request);
}
*/
