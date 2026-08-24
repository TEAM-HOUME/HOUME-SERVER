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
}
