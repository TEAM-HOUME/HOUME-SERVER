package or.sopt.houme.domain.house.model.carousel.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "carousel_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CarouselType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 캐러셀 가구 타입 (ex. 의자, 식탁, 소파 등등)
    @Column(name = "type_name", nullable = false)
    private String typeName;
}
