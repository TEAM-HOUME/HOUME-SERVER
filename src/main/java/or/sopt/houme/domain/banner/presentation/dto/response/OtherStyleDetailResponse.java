package or.sopt.houme.domain.banner.presentation.dto.response;

import java.util.List;

public record OtherStyleDetailResponse(
        String styleName,
        String styleImageUrl,
        String styleDescription,
        List<OtherStyleDetailProductResponse> products
) {

    public static OtherStyleDetailResponse of(
            String styleName,
            String styleImageUrl,
            String styleDescription,
            List<OtherStyleDetailProductResponse> products
    ) {
        return new OtherStyleDetailResponse(styleName, styleImageUrl, styleDescription, products);
    }
}
