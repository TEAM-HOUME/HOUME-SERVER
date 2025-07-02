package or.sopt.houme.domain.house.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.taste.entity.Taste;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class HouseTaste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Taste taste;

    @ManyToOne(fetch = FetchType.LAZY)
    private House house;
}
