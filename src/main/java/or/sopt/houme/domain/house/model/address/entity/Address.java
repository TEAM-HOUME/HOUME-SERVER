package or.sopt.houme.domain.house.model.address.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.presentation.address.dto.request.AddressRequest;

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

    // #582: User 연관 절단 — @ManyToOne 대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id")
    private Long userId;

    // 생성 정적 메서드
    public static Address create(Long userId, AddressRequest request) {
        return Address.builder()
                .sigungu(request.sigungu())
                .roadName(request.roadName())
                .userId(userId)
                .build();
    }
}
