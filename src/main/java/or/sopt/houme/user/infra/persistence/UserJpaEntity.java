package or.sopt.houme.user.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.SocialType;
import or.sopt.houme.domain.user.model.entity.UserStatus;
import or.sopt.houme.global.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 유저 영속 엔티티. 순수 도메인 모델({@link or.sopt.houme.user.domain.User})과 분리된 infra 전용 타입.
 *
 * <p>기존 {@code users} 테이블 스키마와 매핑이 완전히 동일하다. cross-domain 연관은 없다(스칼라/enum 뿐).
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_nickname_nickname_tag",
                columnNames = {"nickname", "nickname_tag"}
        )
)
public class UserJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "nickname", nullable = true)
    private String nickname;

    @Column(name = "nickname_tag", nullable = true, length = 5)
    private String nicknameTag;

    @Column(name = "birthday", nullable = true)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true)
    private Gender gender;

    @Column(name = "email", unique = true, nullable = true)
    private String email;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "has_generated_image")
    private Boolean hasGeneratedImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = true)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

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
        if (!this.hasGeneratedImage) this.hasGeneratedImage = true;
    }

    /** 순수 도메인 모델의 변경 가능 필드를 반영한다 (어댑터 전용, JPA 더티 체킹으로 UPDATE). */
    public void applyDomainState(or.sopt.houme.user.domain.User user) {
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.nicknameTag = user.getNicknameTag();
        this.birthday = user.getBirthday();
        this.gender = user.getGender();
        this.hasGeneratedImage = user.getHasGeneratedImage();
    }
}
