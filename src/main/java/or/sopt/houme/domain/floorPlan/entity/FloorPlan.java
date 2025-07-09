package or.sopt.houme.domain.floorPlan.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "form", nullable = false)
    private Form form;                      // 구조

    @Enumerated(EnumType.STRING)
    @Column(name = "structure", nullable = false)
    private Structure structure;            // 형태

    @Column(name = "floor_plan_image", nullable = false)
    private String floorPlanImage;

    @Column(name = "floor_plan_prompt", nullable = false)
    private String floorPlanPrompt;
}

