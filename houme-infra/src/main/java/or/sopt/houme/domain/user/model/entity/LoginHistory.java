package or.sopt.houme.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "login_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistory extends BaseEntity {
    // 로그인 이력 (createdAt = 로그인 시각)

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인한 유저
    @Comment(value = "로그인한 유저 식별자")
    @Column(nullable = false)
    private Long userId;

    // 로그인 종류 (신규 가입 / 기존 로그인)
    @Comment(value = "로그인 종류 (SIGN_UP / LOGIN)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LoginType loginType;

    // 소셜 종류
    @Comment(value = "소셜 종류 (KAKAO)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialType socialType;

    @Builder
    public LoginHistory(Long userId, LoginType loginType, SocialType socialType) {
        this.userId = userId;
        this.loginType = loginType;
        this.socialType = socialType;
    }
}
