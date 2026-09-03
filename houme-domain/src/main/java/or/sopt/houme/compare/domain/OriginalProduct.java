package or.sopt.houme.compare.domain;

public record OriginalProduct(
        String title,
        String imageUrl,
        Double price,
        String currency,
        String quality,
        String category  // nullable, e.g. "FURNITURE"
) {
    public static OriginalProduct of(String title, String imageUrl, Double price, String category) {
        return new OriginalProduct(title, imageUrl, price, "KRW", "FULL", category);
    }
}
