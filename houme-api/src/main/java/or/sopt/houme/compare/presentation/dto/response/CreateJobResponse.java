package or.sopt.houme.compare.presentation.dto.response;

public record CreateJobResponse(
        String jobId,
        String status,
        String sourceUrl,
        String title,
        String thumbnail,
        Long price,
        String brand
) {}
