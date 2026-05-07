package or.sopt.houme.domain.user.presentation.controller.dto;

import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;

import java.time.LocalDate;

public record UpdateMyPageProfileResponse(
        Long userId,
        String nickname,
        LocalDate birthday,
        Gender gender
) {
    public static UpdateMyPageProfileResponse from(User user) {
        return new UpdateMyPageProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getBirthday(),
                user.getGender()
        );
    }
}
