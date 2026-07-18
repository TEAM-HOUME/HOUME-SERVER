package or.sopt.houme.domain.house.model.entity.mapping;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "house_tastes")
public class HouseTaste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // #582: Taste 연관 절단 — @ManyToOne 대신 taste_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "taste_id")
    private Long tasteId;

    // #582: House 연관 절단 — @ManyToOne 대신 house_id(Long) 컬럼으로만 참조(순수 매핑, FK 는 DB 가 계속 강제)
    @Column(name = "house_id")
    private Long houseId;
}
