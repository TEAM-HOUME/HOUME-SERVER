package or.sopt.houme.domain.banner.presentation.dto.response;

import java.util.List;

public record OtherStyleListResponse(
        List<OtherStyleResponse> otherStyles
) {

    public static OtherStyleListResponse of(List<OtherStyleResponse> otherStyles) {
        return new OtherStyleListResponse(otherStyles);
    }
}
