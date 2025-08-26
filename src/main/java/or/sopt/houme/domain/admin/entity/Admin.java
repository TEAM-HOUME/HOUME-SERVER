package or.sopt.houme.domain.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.entity.Role;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("어드민 회원 이름")
    @Column(name = "username", nullable = false)
    private String username;

    @Comment("어드민 비밀번호")
    @Column(name = "password", nullable = false)
    private String password;

    @Comment("어드민 권한")
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
}
