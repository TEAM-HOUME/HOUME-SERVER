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
    private volatile String ebayStatus = "WAITING";
    private volatile String coupangStatus = "WAITING";
    private volatile String catalogStatus = "WAITING";

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
    public String getEbayStatus() { return ebayStatus; }
    public String getCoupangStatus() { return coupangStatus; }
    public String getCatalogStatus() { return catalogStatus; }

    public synchronized void markRunning(JobStage stage) {
        this.status = JobStatus.RUNNING;
        this.currentStage = stage;
        this.ebayStatus = "RUNNING";
    }

    public synchronized void markEbayDone() { this.ebayStatus = "DONE"; }
    public synchronized void markEbayFailed() { this.ebayStatus = "FAILED"; }
    public synchronized void markCoupangDone() { this.coupangStatus = "DONE"; }
    public synchronized void markCoupangFailed() { this.coupangStatus = "FAILED"; }
    public synchronized void markCatalogDone() { this.catalogStatus = "DONE"; }
    public synchronized void markCatalogFailed() { this.catalogStatus = "FAILED"; }

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

    public static CompareJob restore(String jobId, String sourceUrl, JobStatus status,
            JobStage currentStage, OriginalProduct originalProduct,
            List<SimilarProduct> similarProducts, String errorCode,
            String ebayStatus, String coupangStatus, String catalogStatus) {
        CompareJob job = new CompareJob(jobId, sourceUrl);
        job.status = status;
        job.currentStage = currentStage;
        job.originalProduct = originalProduct;
        job.similarProducts = similarProducts;
        job.errorCode = errorCode;
        job.ebayStatus = ebayStatus != null ? ebayStatus : "WAITING";
        job.coupangStatus = coupangStatus != null ? coupangStatus : "WAITING";
        job.catalogStatus = catalogStatus != null ? catalogStatus : "WAITING";
        return job;
    }
}
