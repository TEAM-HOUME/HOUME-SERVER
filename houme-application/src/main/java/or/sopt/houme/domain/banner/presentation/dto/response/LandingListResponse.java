package or.sopt.houme.domain.banner.presentation.dto.response;

import java.util.List;

public record LandingListResponse(
        List<LandingResponse> landings
) {

    public static LandingListResponse of(List<LandingResponse> landings) {
        return new LandingListResponse(landings);
    }
}
