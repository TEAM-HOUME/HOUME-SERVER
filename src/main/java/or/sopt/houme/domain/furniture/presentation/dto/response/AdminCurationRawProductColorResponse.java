package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;

public record AdminCurationRawProductColorResponse(
        Long id,
        String rawColorName,
        String clientColorName
) {
    public static AdminCurationRawProductColorResponse of(CurationRawProductColor color) {
        return new AdminCurationRawProductColorResponse(
                color.getId(),
                color.getRawColorName(),
                color.getClientColorName()
        );
    }
}
