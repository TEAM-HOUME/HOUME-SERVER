package or.sopt.houme.domain.coupang;

import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangJobStatus;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CoupangSeedJobTest {

    @Test
    @DisplayName("수집 Job은 완료 후 시간 기반 갱신 예약 없이 큐의 뒤로 돌아간다")
    void jobReturnsToQueueTailAfterSuccess() {
        LocalDateTime now = LocalDateTime.now();
        CoupangKeywordJpaEntity keyword = CoupangKeywordJpaEntity.of("3인용 소파", "SOFA");
        CoupangCollectionJobJpaEntity job = CoupangCollectionJobJpaEntity.of(keyword, now.minusMinutes(7));

        job.claim(now.minusMinutes(1));
        job.returnToQueueTail(now);

        assertThat(job.getStatus()).isEqualTo(CoupangJobStatus.PENDING);
        assertThat(job.getScheduledAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("수집 실패 Job은 재시도 없이 오류를 남기고 큐의 뒤로 돌아간다")
    void failedJobReturnsToQueueTailWithoutRetry() {
        LocalDateTime now = LocalDateTime.now();
        CoupangKeywordJpaEntity keyword = CoupangKeywordJpaEntity.of("3인용 소파", "SOFA");
        CoupangCollectionJobJpaEntity job = CoupangCollectionJobJpaEntity.of(keyword, now.minusMinutes(7));

        job.claim(now.minusMinutes(1));
        job.failAndReturnToQueueTail(now, "COUPANG_API_ERROR", "호출 실패");

        assertThat(job.getStatus()).isEqualTo(CoupangJobStatus.PENDING);
        assertThat(job.getScheduledAt()).isEqualTo(now);
        assertThat(job.getErrorCode()).isEqualTo("COUPANG_API_ERROR");
        assertThat(job.getErrorMessage()).isEqualTo("호출 실패");
    }

    @Test
    @DisplayName("1시간 내 완료되지 않은 RUNNING Job은 PENDING으로 복구한다")
    void runningJobIsRecoveredAfterTimeout() {
        LocalDateTime now = LocalDateTime.now();
        CoupangKeywordJpaEntity keyword = CoupangKeywordJpaEntity.of("3인용 소파", "SOFA");
        CoupangCollectionJobJpaEntity job = CoupangCollectionJobJpaEntity.of(keyword, now.minusHours(2));

        job.claim(now.minusHours(1));
        job.recoverFromRunningTimeout(now);

        assertThat(job.getStatus()).isEqualTo(CoupangJobStatus.PENDING);
        assertThat(job.getScheduledAt()).isEqualTo(now);
        assertThat(job.getErrorCode()).isEqualTo("RUNNING_TIMEOUT");
    }
}
