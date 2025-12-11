package or.sopt.houme.domain.user.controller.dto;

import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;

public record UserImageHistoryDTO(
        Long houseId,   // houseId
        Long imageId,   // imageId
        String generatedImageUrl,
        String tasteTag,
        String equilibrium,
        String houseForm,
        boolean isMirror
) {
    public static UserImageHistoryDTO of(Long houseId, Long imageId, String generatedImageUrl, String tasteTag, String equilibrium, String houseForm, boolean isMirror) {
        return new UserImageHistoryDTO(houseId, imageId, generatedImageUrl, tasteTag, equilibrium, houseForm, isMirror);
    }

    // ENUM타입으로 받아 String으로 저장
    public UserImageHistoryDTO(Long houseId, Long imageId, String generatedImageUrl, String tasteTag, Equilibrium equilibrium, Form form, boolean isMirror) {
        this(houseId, imageId, generatedImageUrl, tasteTag, equilibrium.getDescription(), form.getDescription(),isMirror);
    }
}
