package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.enums.Activity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "activity_furnitures",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_activity_furnitures_activity_furniture",
                        columnNames = {"activity", "furniture_id"}
                )
        }
)
public class ActivityFurniture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture;

    @Column(name = "priority", nullable = false)
    private int priority;
}
