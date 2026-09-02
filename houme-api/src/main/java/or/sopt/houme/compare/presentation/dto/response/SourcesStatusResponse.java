package or.sopt.houme.compare.presentation.dto.response;

public record SourcesStatusResponse(
        String ebay,
        String coupang,
        String catalog
) {
    public static SourcesStatusResponse of(String ebayStatus) {
        return new SourcesStatusResponse(ebayStatus, ebayStatus, ebayStatus);
    }
}
