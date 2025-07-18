package or.sopt.houme.domain.user.controller.dto;

public record ImageHistoryResultPageResponse(
        String equilibrium,
        String houseForm,
        String tasteTag,
        String name,
        String generatedImageUrl,
        boolean isLike
) {
    public static ImageHistoryResultPageResponse of(
            String equilibrium,
            String houseForm,
            String tasteTag,
            String name,
            String generatedImageUrl,
            boolean isLike
    ) {
        return new ImageHistoryResultPageResponse(equilibrium, houseForm, tasteTag, name, generatedImageUrl, isLike);
    }
}
