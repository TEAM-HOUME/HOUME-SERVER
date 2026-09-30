package or.sopt.houme.compare.application;

public interface AdminCurationEmbeddingUseCase {
    void triggerEmbeddingAsync(int limit);
    boolean isRunning();
}
