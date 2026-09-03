package or.sopt.houme.compare.application.dto;

import java.time.OffsetDateTime;

public record CompareJobHistoryItemResponse(
        String sourceUrl,
        String thumbnailUrl,
        String title,
        Double price,
        String currency,
        OffsetDateTime createdAt
) {}
