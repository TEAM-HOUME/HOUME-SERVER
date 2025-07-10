package or.sopt.houme.domain.user.controller.dto;

public record ImageHistoryResultPageResponse(
        String equilibrium,
        String houseForm,
        String tasteTag,
        String name,
        String generatedImageUrl
) {
    public static ImageHistoryResultPageResponse of(
            String equilibrium,
            String houseForm,
            String tasteTag,
            String name,
            String generatedImageUrl
    ) {
        return new ImageHistoryResultPageResponse(equilibrium, houseForm, tasteTag, name, generatedImageUrl);
    }
}
