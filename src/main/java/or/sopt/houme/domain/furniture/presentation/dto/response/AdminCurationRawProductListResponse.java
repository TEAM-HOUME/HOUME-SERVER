package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record AdminCurationRawProductListResponse(
        List<AdminCurationRawProductResponse> products,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
