package or.sopt.houme.compare.application.dto;

import or.sopt.houme.compare.domain.CompareHistoryItem;

import java.time.LocalDateTime;
import java.util.List;

public record CompareHistoryResponse(List<HistoryItem> items) {

    public record HistoryItem(
            String sourceUrl,
            String thumbnailUrl,
            String title,
            Long price,
            String currency,
            LocalDateTime createdAt
    ) {}

    public static CompareHistoryResponse from(List<CompareHistoryItem> items) {
        return new CompareHistoryResponse(
                items.stream()
                        .map(i -> new HistoryItem(
                                i.sourceUrl(),
                                i.thumbnailUrl(),
                                i.title(),
                                i.price(),
                                i.price() != null ? "KRW" : null,
                                i.createdAt()
                        ))
                        .toList()
        );
    }
}
