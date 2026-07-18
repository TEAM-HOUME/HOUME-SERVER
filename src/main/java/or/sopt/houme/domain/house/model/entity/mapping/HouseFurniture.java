package or.sopt.houme.domain.house.model.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.House;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "house_furnitures")
public class HouseFurniture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // #582: Furniture 연관 절단 — @ManyToOne 대신 furniture_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "furniture_id")
    private Long furnitureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;
}
