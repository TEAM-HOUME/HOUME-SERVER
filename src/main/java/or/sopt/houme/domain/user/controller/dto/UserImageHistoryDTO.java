package or.sopt.houme.domain.user.controller.dto;

public record UserImageHistoryDTO(
        String generatedImageUrl,
        String tasteTag,
        String equilibrium,
        String houseForm
) {
    public static UserImageHistoryDTO of(String generatedImageUrl, String tasteTag, String equilibrium, String houseForm) {
        return new UserImageHistoryDTO(generatedImageUrl, tasteTag, equilibrium, houseForm);
    }
}
