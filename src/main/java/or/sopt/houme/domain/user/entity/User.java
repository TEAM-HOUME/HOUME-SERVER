package or.sopt.houme.domain.user.entity;
import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.house.entity.House;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Date birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Credit> credits = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<House> houses = new ArrayList<>();

    // 회원의 정적팩터링 메서드는 아직 구현하지 않았습니다
}
