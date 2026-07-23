package or.sopt.houme.user.infra.persistence;

import or.sopt.houme.user.domain.User;

/**
 * 유저 영속 엔티티 ↔ 순수 도메인 모델 매퍼.
 */
final class UserMapper {

    private UserMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getNickname(),
                entity.getNicknameTag(),
                entity.getBirthday(),
                entity.getGender(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getHasGeneratedImage(),
                entity.getSocialType(),
                entity.getStatus(),
                entity.getRole()
        );
    }

    static UserJpaEntity toNewEntity(User user) {
        return UserJpaEntity.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .nicknameTag(user.getNicknameTag())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .email(user.getEmail())
                .password(user.getPassword())
                .hasGeneratedImage(user.getHasGeneratedImage())
                .socialType(user.getSocialType())
                .status(user.getStatus())
                .role(user.getRole())
                .build();
    }
}
