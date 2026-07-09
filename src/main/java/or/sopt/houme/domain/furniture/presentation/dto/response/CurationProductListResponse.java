package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record CurationProductListResponse(
        List<CurationProductResponse> products,
        CurationProductMetaResponse meta
) {
}
