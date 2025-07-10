package or.sopt.houme.domain.user.controller.dto;

import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;

public record UserImageHistoryDTO(
        String generatedImageUrl,
        String tasteTag,
        String equilibrium,
        String houseForm
) {
    public static UserImageHistoryDTO of(String generatedImageUrl, String tasteTag, String equilibrium, String houseForm) {
        return new UserImageHistoryDTO(generatedImageUrl, tasteTag, equilibrium, houseForm);
    }

    // ENUM타입으로 받아 String으로 저장
    public UserImageHistoryDTO(String generatedImageUrl, String tasteTag, Equilibrium equilibrium, Form form) {
        this(generatedImageUrl, tasteTag, equilibrium.toString(), form.toString());
    }
}
