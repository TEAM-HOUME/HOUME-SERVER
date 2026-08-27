package or.sopt.houme.compare.domain;

public record EbayCandidate(
        String itemId,
        String title,
        double priceUsd,
        String thumbnailUrl,
        String itemWebUrl,
        java.util.List<String> categoryIds
) {}
