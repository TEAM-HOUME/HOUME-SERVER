package or.sopt.houme.domain.house.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.house.entity.House;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id")
    private Furniture furniture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;
}
