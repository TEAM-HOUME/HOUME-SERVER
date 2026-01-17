package or.sopt.houme.domain.preference.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.carousel.entity.Carousel;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "carousel_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "carousel_id"}))
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

    @Column(name = "user_id")
    private Long userId;

    public static CarouselPreference of(Preference preference, Carousel carousel,Long userId) {
        return CarouselPreference.builder()
                .preference(preference)
                .carousel(carousel)
                .userId(userId)
                .build();
    }

}
