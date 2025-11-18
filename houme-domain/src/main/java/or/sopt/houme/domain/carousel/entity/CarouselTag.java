package or.sopt.houme.domain.carousel.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.taste.entity.Tag;

@Getter
@Entity
@Table(name = "carousel_tags")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CarouselTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carousel_id")
    private Carousel carousel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
