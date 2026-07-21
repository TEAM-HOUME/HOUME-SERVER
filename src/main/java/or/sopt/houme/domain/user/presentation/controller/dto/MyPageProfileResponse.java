package or.sopt.houme.domain.user.presentation.controller.dto;

import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;

import java.time.LocalDate;

public record MyPageProfileResponse(
        Long userId,
        String nickname,
        LocalDate birthday,
        Gender gender
) {
    public static MyPageProfileResponse from(UserJpaEntity user) {
        return new MyPageProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getBirthday(),
                user.getGender()
        );
    }
}
