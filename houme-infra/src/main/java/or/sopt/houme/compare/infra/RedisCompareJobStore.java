package or.sopt.houme.compare.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.JobStage;
import or.sopt.houme.compare.domain.JobStatus;
import or.sopt.houme.compare.domain.OriginalProduct;
import or.sopt.houme.compare.domain.SimilarProduct;
import or.sopt.houme.compare.domain.port.out.CompareJobStorePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCompareJobStore implements CompareJobStorePort {

    private static final String JOB_PREFIX = "compare-job:";
    private static final String URL_PREFIX  = "compare-job:active-url:";
    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(CompareJob job) {
        try {
            String json = objectMapper.writeValueAsString(Dto.from(job));
            stringRedisTemplate.opsForValue().set(JOB_PREFIX + job.getJobId(), json, TTL);

            if (job.getSourceUrl() != null) {
                String urlKey = URL_PREFIX + job.getSourceUrl();
                if (job.getStatus() == JobStatus.PENDING || job.getStatus() == JobStatus.RUNNING) {
                    stringRedisTemplate.opsForValue().set(urlKey, job.getJobId(), TTL);
                } else {
                    stringRedisTemplate.delete(urlKey);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("CompareJob 직렬화 실패: jobId={}", job.getJobId(), e);
        }
    }

    @Override
    public Optional<CompareJob> findById(String jobId) {
        String json = stringRedisTemplate.opsForValue().get(JOB_PREFIX + jobId);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, Dto.class).toJob());
        } catch (JsonProcessingException e) {
            log.error("CompareJob 역직렬화 실패: jobId={}", jobId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompareJob> findActiveBySourceUrl(String sourceUrl) {
        if (sourceUrl == null) return Optional.empty();
        String jobId = stringRedisTemplate.opsForValue().get(URL_PREFIX + sourceUrl);
        if (jobId == null) return Optional.empty();
        return findById(jobId);
    }

    private record Dto(
            String jobId,
            String sourceUrl,
            String status,
            String currentStage,
            OriginalProduct originalProduct,
            List<SimilarProduct> similarProducts,
            String errorCode
    ) {
        static Dto from(CompareJob job) {
            return new Dto(
                    job.getJobId(),
                    job.getSourceUrl(),
                    job.getStatus().name(),
                    job.getCurrentStage() != null ? job.getCurrentStage().name() : null,
                    job.getOriginalProduct(),
                    job.getSimilarProducts(),
                    job.getErrorCode()
            );
        }

        CompareJob toJob() {
            return CompareJob.restore(
                    jobId, sourceUrl,
                    JobStatus.valueOf(status),
                    currentStage != null ? JobStage.valueOf(currentStage) : null,
                    originalProduct, similarProducts, errorCode
            );
        }
    }
}
