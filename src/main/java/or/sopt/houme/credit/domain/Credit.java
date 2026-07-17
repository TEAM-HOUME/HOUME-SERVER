package or.sopt.houme.credit.domain;

import java.time.LocalDateTime;

/**
 * 크레딧 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 *
 * <p>잔액은 "1 크레딧 = 1 레코드" 모델이므로, 이 모델은 개별 크레딧 1건을 표현한다.
 * 상태 전이 규칙(ACTIVE→PENDING 예약, PENDING→ACTIVE 복구)을 도메인이 소유한다.
 */
public class Credit {

    private final Long id;
    private final Long userId;
    private CreditStatus status;
    private final LocalDateTime createdAt;

    private Credit(Long id, Long userId, CreditStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** 신규 발급 (아직 영속화 전이므로 id/createdAt 없음). */
    public static Credit issue(Long userId) {
        return new Credit(null, userId, CreditStatus.ACTIVE, null);
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static Credit reconstitute(Long id, Long userId, CreditStatus status, LocalDateTime createdAt) {
        return new Credit(id, userId, status, createdAt);
    }

    /** ACTIVE → PENDING 예약. 이미지 생성 시작 시 사용. */
    public void reserve() {
        if (status != CreditStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 크레딧만 예약할 수 있습니다. 현재 상태=" + status);
        }
        this.status = CreditStatus.PENDING;
    }

    /** PENDING → ACTIVE 복구. 이미지 생성 실패 시 사용. */
    public void restore() {
        if (status == CreditStatus.PENDING) {
            this.status = CreditStatus.ACTIVE;
        }
    }

    public boolean isPending() {
        return status == CreditStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public CreditStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
