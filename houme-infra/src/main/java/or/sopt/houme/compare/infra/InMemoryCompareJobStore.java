package or.sopt.houme.compare.infra;

import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.JobStatus;
import or.sopt.houme.compare.domain.port.out.CompareJobStorePort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCompareJobStore implements CompareJobStorePort {

    // ponytail: in-memory, single-node only — replace with Redis if multi-instance
    private final ConcurrentHashMap<String, CompareJob> store = new ConcurrentHashMap<>();

    @Override
    public void save(CompareJob job) {
        store.put(job.getJobId(), job);
    }

    @Override
    public Optional<CompareJob> findById(String jobId) {
        return Optional.ofNullable(store.get(jobId));
    }

    @Override
    public Optional<CompareJob> findActiveBySourceUrl(String sourceUrl) {
        if (sourceUrl == null) return Optional.empty();
        return store.values().stream()
                .filter(j -> sourceUrl.equals(j.getSourceUrl())
                        && (j.getStatus() == JobStatus.PENDING || j.getStatus() == JobStatus.RUNNING))
                .findFirst();
    }
}
