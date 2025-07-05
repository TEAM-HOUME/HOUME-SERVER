package or.sopt.houme.domain.house.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.floorPlan.entity.FloorPlans;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HouseFloorPlans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_plan_id")
    private FloorPlans floorPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private Houses house;

    @Column(name = "is_reverse", nullable = false)
    private boolean isReverse;
}
