package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "furniture_recommend_btn_click_logs")
public class FurnitureRecommendBtnClickLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // #582: User 연관 절단 —  대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id")
    private Long userId;

    public static FurnitureRecommendBtnClickLog of(Long userId) {
        return FurnitureRecommendBtnClickLog.builder()
                .userId(userId)
                .build();
    }
}
