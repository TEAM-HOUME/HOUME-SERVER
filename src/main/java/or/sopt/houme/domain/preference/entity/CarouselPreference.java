package or.sopt.houme.domain.preference.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.carousel.entity.Carousel;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class CarouselPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id")
    private Preference preference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carousel_id")
    private Carousel carousel;
}
