package or.sopt.houme.domain.house.model.entity.mapping;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.taste.entity.Taste;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taste_id")
    private Taste taste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;
}
