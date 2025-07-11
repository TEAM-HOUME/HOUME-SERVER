package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "furniture_types")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FurnitureType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "furniture_type", nullable = false)
    private FurnitureTypes furnitureType;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;
}
