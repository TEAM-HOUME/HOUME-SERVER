package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record JjymV2ListResponse(List<JjymV2ItemResponse> items) {
    public static JjymV2ListResponse of(List<JjymV2ItemResponse> items) {
        return new JjymV2ListResponse(items);
    }
}
