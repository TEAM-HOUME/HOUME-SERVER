package or.sopt.houme.domain.user.controller.dto;

import java.util.List;

public record UserImageHistoryListResponse(List<UserImageHistoryDTO> histories) {

    public static UserImageHistoryListResponse of(List<UserImageHistoryDTO> histories) {
        return new UserImageHistoryListResponse(histories);
    }
}
