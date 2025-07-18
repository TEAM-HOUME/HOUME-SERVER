package or.sopt.houme.domain.user.controller.dto;

import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;

public record UserImageHistoryDTO(
        Long imageId,
        String generatedImageUrl,
        String tasteTag,
        String equilibrium,
        String houseForm
) {
    public static UserImageHistoryDTO of(Long imageId, String generatedImageUrl, String tasteTag, String equilibrium, String houseForm) {
        return new UserImageHistoryDTO(imageId, generatedImageUrl, tasteTag, equilibrium, houseForm);
    }

    // ENUM타입으로 받아 String으로 저장
    public UserImageHistoryDTO(Long imageId, String generatedImageUrl, String tasteTag, Equilibrium equilibrium, Form form) {
        this(imageId, generatedImageUrl, tasteTag, equilibrium.getDescription(), form.getDescription());
    }
}
