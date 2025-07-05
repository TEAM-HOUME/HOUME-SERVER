package or.sopt.houme.domain.floorPlan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "floor_plans")
public class FloorPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "floor_plan_image", nullable = false)
    private String floorPlanImage;

    @Column(name = "floor_plan_prompt", nullable = false)
    private String floorPlanPrompt;
}
