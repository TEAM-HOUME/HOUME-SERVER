package or.sopt.houme.domain.house.model.carousel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "carousel_like_logs")
public class CarouselLikeLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // #582: User 연관 절단 —  대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    private CurationRawProduct curationRawProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private CarouselLikeLogAction action;

    public static CarouselLikeLog of(Long userId, CurationRawProduct curationRawProduct, CarouselLikeLogAction action) {
        return CarouselLikeLog.builder()
                .userId(userId)
                .curationRawProduct(curationRawProduct)
                .action(action)
                .build();
    }
}
