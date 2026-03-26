package or.sopt.houme.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.global.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(name = "users")
public class User extends BaseEntity {

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
}
