package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static FurnitureRecommendBtnClickLog of(User user) {
        return FurnitureRecommendBtnClickLog.builder()
                .user(user)
                .build();
    }
}
