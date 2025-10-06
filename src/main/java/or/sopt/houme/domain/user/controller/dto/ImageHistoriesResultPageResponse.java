package or.sopt.houme.domain.user.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
            @Schema(description = "좋아요 여부", nullable = true)
            Boolean isLike,
            @Schema(description = "선호도 요인 식별자", nullable = true)
            Long factorId,
            @Schema(description = "선호도 요인", nullable = true)
            String factorText
    ) {
        public static ImageHistoryResultPageResponse of(
                String equilibrium,
                String houseForm,
                String tasteTag,
                String name,
                String generatedImageUrl,
                Boolean isLike,
                Long factorId,
                String factorText
        ) {
            return new ImageHistoryResultPageResponse(
                    equilibrium, houseForm, tasteTag, name, generatedImageUrl, isLike, factorId, factorText
            );
        }
    }
}
