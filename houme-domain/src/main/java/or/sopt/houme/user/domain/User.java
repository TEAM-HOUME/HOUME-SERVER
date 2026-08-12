package or.sopt.houme.user.domain;

import lombok.Builder;
import lombok.Getter;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.SocialType;
import or.sopt.houme.domain.user.model.entity.UserStatus;

import java.time.LocalDate;

/**
 * 유저 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 *
 * <p>영속 엔티티({@link or.sopt.houme.user.infra.persistence.UserJpaEntity})와 분리된 도메인 전용 타입.
 * 프로필 갱신 규칙(닉네임 변경 시 name 동기화 등)을 도메인이 소유한다.
 * enum(Gender/Role/SocialType/UserStatus)은 순수 타입이라 기존 패키지를 공유한다(P2 물리 분리 시 domain 모듈로 이동).
 */
@Getter
@Builder
public class User {

    private final Long id;
    private String name;
    private String nickname;
    private String nicknameTag;
    private LocalDate birthday;
    private Gender gender;
    private final String email;
    private final String password;
    private Boolean hasGeneratedImage;
    private final SocialType socialType;
    private final UserStatus status;
    private final Role role;

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static User reconstitute(Long id, String name, String nickname, String nicknameTag,
                                    LocalDate birthday, Gender gender, String email, String password,
                                    Boolean hasGeneratedImage, SocialType socialType, UserStatus status, Role role) {
        return User.builder()
                .id(id)
                .name(name)
                .nickname(nickname)
                .nicknameTag(nicknameTag)
                .birthday(birthday)
                .gender(gender)
                .email(email)
                .password(password)
                .hasGeneratedImage(hasGeneratedImage)
                .socialType(socialType)
                .status(status)
                .role(role)
                .build();
    }

    // v1 자체 회원가입시 사용되는 유저 업데이트 메서드
    public void updateUserFromSignUp(String name, LocalDate birthday, Gender gender) {
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
    }

    // v2 자체 회원가입시 사용되는 유저 업데이트 메서드
    public void updateUserFromSignUpV2(String nickname, String nicknameTag, LocalDate birthday, Gender gender) {
        this.nickname = nickname;
        this.nicknameTag = nicknameTag;
        this.name = nickname;
        this.birthday = birthday;
        this.gender = gender;
    }

    public void updateMyPageProfile(String nickname, String nicknameTag, LocalDate birthday, Gender gender) {
        if (nickname != null) {
            this.nickname = nickname;
            this.nicknameTag = nicknameTag;
            this.name = nickname;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
        if (gender != null) {
            this.gender = gender;
        }
    }

    public String getDisplayName() {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        return "";
    }

    // 이미지 생성 여부 update
    public void updateHasGeneratedImage() {
        if (hasGeneratedImage == null || !hasGeneratedImage) {
            this.hasGeneratedImage = true;
        }
    }
}
