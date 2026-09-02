package or.sopt.houme.compare.presentation.dto.response;

import java.time.OffsetDateTime;

public record CompareJobHistoryItemResponse(
        String sourceUrl,
        String thumbnailUrl,
        String title,
        Double price,
        String currency,
        OffsetDateTime createdAt
) {}
