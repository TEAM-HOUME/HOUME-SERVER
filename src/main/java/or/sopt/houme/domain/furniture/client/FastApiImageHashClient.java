package or.sopt.houme.domain.furniture.client;

import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.ImageHashRequest;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "imageHashClient",
        url = "${external.image-api.base-url}"
)
public interface FastApiImageHashClient {

    @PostMapping(
            value = "/imagehash/similarity",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    SimilarityResponse getTopKSimilarImages(@RequestBody ImageHashRequest request);
}
