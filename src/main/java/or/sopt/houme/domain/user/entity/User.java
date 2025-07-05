package or.sopt.houme.domain.user.entity;
import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.house.entity.House;
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

    @Column(name = "birthday", nullable = true)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true)
    private Gender gender;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Credit> credits = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<House> houses = new ArrayList<>();

    // 회원의 정적팩터링 메서드는 아직 구현하지 않았습니다
}
