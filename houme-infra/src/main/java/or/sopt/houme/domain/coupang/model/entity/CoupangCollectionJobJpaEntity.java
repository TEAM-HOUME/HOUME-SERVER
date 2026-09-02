package or.sopt.houme.domain.coupang.model.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupang_collection_jobs")
@Comment("쿠팡 상품 수집 영속 작업 큐")
public class CoupangCollectionJobJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private CoupangKeywordJpaEntity keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CoupangJobStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    /** 사용자 요청으로 새로 등록된 Job은 다음 선점 전까지 일반 순환 Job보다 먼저 처리한다. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean priority;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(length = 100)
    private String errorCode;

    @Column(length = 1000)
    private String errorMessage;

    private CoupangCollectionJobJpaEntity(CoupangKeywordJpaEntity keyword, LocalDateTime scheduledAt) {
        this.keyword = keyword;
        this.status = CoupangJobStatus.PENDING;
        this.scheduledAt = scheduledAt;
        this.priority = false;
    }

    public static CoupangCollectionJobJpaEntity of(CoupangKeywordJpaEntity keyword, LocalDateTime scheduledAt) {
        return new CoupangCollectionJobJpaEntity(keyword, scheduledAt);
    }

    public static CoupangCollectionJobJpaEntity priorityOf(CoupangKeywordJpaEntity keyword, LocalDateTime scheduledAt) {
        CoupangCollectionJobJpaEntity job = new CoupangCollectionJobJpaEntity(keyword, scheduledAt);
        job.priority = true;
        return job;
    }

    public void claim(LocalDateTime now) {
        this.status = CoupangJobStatus.RUNNING;
        this.startedAt = now;
        this.priority = false;
    }

    public void failAndReturnToQueueTail(LocalDateTime now, String errorCode, String errorMessage) {
        this.status = CoupangJobStatus.PENDING;
        this.scheduledAt = now;
        this.finishedAt = now;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void returnToQueueTail(LocalDateTime now) {
        this.status = CoupangJobStatus.PENDING;
        this.scheduledAt = now;
        this.finishedAt = now;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void recoverFromRunningTimeout(LocalDateTime now) {
        this.status = CoupangJobStatus.PENDING;
        this.scheduledAt = now;
        this.finishedAt = now;
        this.errorCode = "RUNNING_TIMEOUT";
        this.errorMessage = "1시간 내에 완료되지 않은 RUNNING 작업을 복구했습니다.";
    }
}
