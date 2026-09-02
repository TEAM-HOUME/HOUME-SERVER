package or.sopt.houme.domain.coupang.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class CoupangCollectionJobPriorityMigrationRunnerTest {

    @Test
    @DisplayName("priority 컬럼은 nullable 추가 후 기존 행을 백필하고 NOT NULL로 전환한다")
    void migratesPriorityColumnInSafeOrder() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        CoupangCollectionJobPriorityMigrationRunner runner = new CoupangCollectionJobPriorityMigrationRunner(jdbcTemplate);

        runner.run(mock(ApplicationArguments.class));

        var inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE coupang_collection_jobs ADD COLUMN IF NOT EXISTS priority boolean");
        inOrder.verify(jdbcTemplate).execute("UPDATE coupang_collection_jobs SET priority = false WHERE priority IS NULL");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE coupang_collection_jobs ALTER COLUMN priority SET DEFAULT false");
        inOrder.verify(jdbcTemplate).execute("ALTER TABLE coupang_collection_jobs ALTER COLUMN priority SET NOT NULL");
    }
}
