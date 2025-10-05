package or.sopt.houme.domain.user.controller.dto;

import java.util.List;

public record ImageHistoriesResultPageResponse(
        List<ImageHistoryResultPageResponse> histories
) {
    public static ImageHistoriesResultPageResponse of(List<ImageHistoryResultPageResponse> histories) {
        return new ImageHistoriesResultPageResponse(histories);
    }

    public record ImageHistoryResultPageResponse(
            String equilibrium,
            String houseForm,
            String tasteTag,
            String name,
            String generatedImageUrl,
            Boolean isLike
    ) {
        public static ImageHistoryResultPageResponse of(
                String equilibrium,
                String houseForm,
                String tasteTag,
                String name,
                String generatedImageUrl,
                Boolean isLike
        ) {
            return new ImageHistoryResultPageResponse(
                    equilibrium, houseForm, tasteTag, name, generatedImageUrl, isLike
            );
        }
    }
}
