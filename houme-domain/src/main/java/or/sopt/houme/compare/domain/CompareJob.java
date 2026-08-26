package or.sopt.houme.compare.domain;

import java.util.List;

public class CompareJob {

    private final String jobId;
    private final String sourceUrl; // null if dummy mode
    private volatile JobStatus status;
    private volatile JobStage currentStage;
    private volatile OriginalProduct originalProduct;
    private volatile List<SimilarProduct> similarProducts;
    private volatile String errorCode;

    public CompareJob(String jobId, String sourceUrl) {
        this.jobId = jobId;
        this.sourceUrl = sourceUrl;
        this.status = JobStatus.PENDING;
        this.currentStage = null;
    }

    public String getJobId() { return jobId; }
    public String getSourceUrl() { return sourceUrl; }
    public JobStatus getStatus() { return status; }
    public JobStage getCurrentStage() { return currentStage; }
    public OriginalProduct getOriginalProduct() { return originalProduct; }
    public List<SimilarProduct> getSimilarProducts() { return similarProducts; }
    public String getErrorCode() { return errorCode; }

    public synchronized void markRunning(JobStage stage) {
        this.status = JobStatus.RUNNING;
        this.currentStage = stage;
    }

    public synchronized void advanceStage(JobStage stage) {
        this.currentStage = stage;
    }

    public synchronized void setOriginalProduct(OriginalProduct product) {
        this.originalProduct = product;
    }

    public synchronized void markDone(List<SimilarProduct> results) {
        this.similarProducts = results;
        this.status = JobStatus.DONE;
    }

    public synchronized void markFailed(String errorCode) {
        this.errorCode = errorCode;
        this.status = JobStatus.FAILED;
    }
}
