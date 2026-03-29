package or.sopt.houme.domain.furniture.presentation.dto.response;

public record PriceRangeFilterResponse(
        String id,
        String label,
        Long min,
        Long max
) {
}
