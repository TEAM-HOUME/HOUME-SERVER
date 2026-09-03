package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.JobStatus;

import java.util.Optional;

public interface CompareJobStorePort {

    void save(CompareJob job);

    Optional<CompareJob> findById(String jobId);

    /**
     * PENDING 또는 RUNNING 상태의 동일 sourceUrl job 반환
     */
    Optional<CompareJob> findActiveBySourceUrl(String sourceUrl);
}
