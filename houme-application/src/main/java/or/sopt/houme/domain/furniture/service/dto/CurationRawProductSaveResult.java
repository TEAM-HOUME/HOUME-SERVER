package or.sopt.houme.domain.furniture.service.dto;

public record CurationRawProductSaveResult(
        int insertedCount,
        int updatedCount,
        int skippedCount
) {
    public static CurationRawProductSaveResult empty() {
        return new CurationRawProductSaveResult(0, 0, 0);
    }

    public int totalSaved() {
        return insertedCount + updatedCount;
    }
}
