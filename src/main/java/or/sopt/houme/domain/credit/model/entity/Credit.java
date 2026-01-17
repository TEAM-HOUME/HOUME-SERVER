package or.sopt.houme.domain.credit.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "credits")
public class Credit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CreditStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * ===도메인 로직===
     */

    // 크레딧 상태 업데이트 메서드
    public void updateStatus(CreditStatus status) {
        this.status = status;
    }
}
