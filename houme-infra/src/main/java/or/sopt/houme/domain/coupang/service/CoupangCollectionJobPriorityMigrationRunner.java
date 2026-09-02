package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** ddl-auto:update 환경에서 priority 컬럼을 기존 Job 데이터에 안전하게 추가합니다. */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class CoupangCollectionJobPriorityMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE coupang_collection_jobs ADD COLUMN IF NOT EXISTS priority boolean");
        jdbcTemplate.execute("UPDATE coupang_collection_jobs SET priority = false WHERE priority IS NULL");
        jdbcTemplate.execute("ALTER TABLE coupang_collection_jobs ALTER COLUMN priority SET DEFAULT false");
        jdbcTemplate.execute("ALTER TABLE coupang_collection_jobs ALTER COLUMN priority SET NOT NULL");
        log.info("쿠팡 수집 Job 우선순위 컬럼 초기화 완료");
    }
}
