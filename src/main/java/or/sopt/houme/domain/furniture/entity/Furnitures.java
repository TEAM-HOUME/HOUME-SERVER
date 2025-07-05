package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Furnitures {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", nullable = false)
    private BedType bedType;

    @Enumerated(EnumType.STRING)
    @Column(name = "closet_type", nullable = false)
    private ClosetType closetType;


    @Enumerated(EnumType.STRING)
    @Column(name = "selective_furniture_type", nullable = false)
    private SelectiveFurnitureType selectiveFurnitureType;
}
