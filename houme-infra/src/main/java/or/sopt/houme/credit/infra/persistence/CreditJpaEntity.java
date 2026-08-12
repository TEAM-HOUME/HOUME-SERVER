package or.sopt.houme.credit.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.credit.domain.CreditStatus;
import or.sopt.houme.global.entity.BaseEntity;

/**
 * 크레딧 영속 엔티티. 도메인 모델({@link or.sopt.houme.credit.domain.Credit})과 분리된 infra 전용 타입.
 *
 * <p>기존 {@code credits} 테이블 스키마(id, status, user_id, created_at, updated_at)와 매핑이 완전히 동일하다.
 * user는 @ManyToOne 대신 user_id 컬럼(Long)으로만 참조해 도메인 순수성을 유지한다(FK는 DB가 계속 강제).
 */
@Entity
@Table(name = "credits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CreditStatus status;

    @Column(name = "user_id")
    private Long userId;

    private CreditJpaEntity(Long id, CreditStatus status, Long userId) {
        this.id = id;
        this.status = status;
        this.userId = userId;
    }

    /** 신규 발급용 (id 없음 → INSERT). */
    public static CreditJpaEntity forInsert(Long userId, CreditStatus status) {
        return new CreditJpaEntity(null, status, userId);
    }

    public void updateStatus(CreditStatus status) {
        this.status = status;
    }
}
