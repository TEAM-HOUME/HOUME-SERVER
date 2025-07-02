package or.sopt.houme.domain.house.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.furniture.entity.Furniture;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HouseFurniture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Furniture furniture;

    @ManyToOne(fetch = FetchType.LAZY)
    private House house;
}
