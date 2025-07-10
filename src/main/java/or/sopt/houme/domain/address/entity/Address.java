package or.sopt.houme.domain.address.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.entity.User;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "addresses")
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String sigungu;         // 시군구

    @Column(nullable = false)
    private String roadName;        // 도로명 주소

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
