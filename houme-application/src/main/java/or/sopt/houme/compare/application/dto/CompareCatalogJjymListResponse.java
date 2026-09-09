package or.sopt.houme.compare.application.dto;

import java.util.List;

public record CompareCatalogJjymListResponse(List<CompareCatalogJjymItemResponse> items) {
    public static CompareCatalogJjymListResponse of(List<CompareCatalogJjymItemResponse> items) {
        return new CompareCatalogJjymListResponse(items);
    }
}
