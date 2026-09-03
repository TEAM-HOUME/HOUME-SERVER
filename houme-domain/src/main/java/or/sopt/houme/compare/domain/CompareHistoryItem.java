package or.sopt.houme.compare.domain;

import java.time.LocalDateTime;

public record CompareHistoryItem(
        String sourceUrl,
        String thumbnailUrl,
        String title,
        Long price,
        LocalDateTime createdAt
) {}
