package or.sopt.houme.compare.presentation.dto.response;

public record PresetOriginalProductResponse(
        String sourceUrl,
        String title,
        String thumbnailUrl,
        String brand,
        Long price,
        String currency
) {}
