package or.sopt.houme.domain.credit.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "payment_btn_click_logs")
public class PaymentBtnClickLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // #582: User 연관 절단 — @ManyToOne 대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id")
    private Long userId;

    public static PaymentBtnClickLog of(Long userId) {
        return PaymentBtnClickLog.builder()
                .userId(userId)
                .build();
    }
}
