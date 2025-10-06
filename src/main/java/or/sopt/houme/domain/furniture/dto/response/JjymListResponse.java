package or.sopt.houme.domain.furniture.dto.response;

import java.util.List;

public record JjymListResponse(List<JjymItemResponse> items) {
    public static JjymListResponse of(List<JjymItemResponse> items) {
        return new JjymListResponse(items);
    }
}

