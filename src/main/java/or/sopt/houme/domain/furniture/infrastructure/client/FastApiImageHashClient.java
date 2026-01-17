package or.sopt.houme.domain.furniture.infrastructure.client;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash.ImageHashRequest;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash.SimilarityResponse;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash.forPlan.ImageHashRequestForPlan;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.fastApiImagehash.forPlan.SimilarityResponseForPlan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "imageHashClient",
        url = "${external.image-api.base-url}",
        primary = false
)
public interface FastApiImageHashClient {

    @PostMapping(
            value = "/imagehash/similarity",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    SimilarityResponse getTopKSimilarImages(@RequestBody ImageHashRequest request);

    @PostMapping(
            value = "/imagehash/similarity/for-plan",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    SimilarityResponseForPlan getTopKSimilarImagesForPlan(@RequestBody ImageHashRequestForPlan request);
}
