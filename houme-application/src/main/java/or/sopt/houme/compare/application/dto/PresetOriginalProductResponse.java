package or.sopt.houme.compare.application.dto;

public record PresetOriginalProductResponse(
        String sourceUrl,
        String title,
        String thumbnailUrl,
        String brand,
        Long price,
        String currency
) {}
