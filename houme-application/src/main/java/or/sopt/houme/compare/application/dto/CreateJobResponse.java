package or.sopt.houme.compare.application.dto;

public record CreateJobResponse(
        String jobId,
        String status,
        String sourceUrl,
        String title,
        String thumbnail,
        Long price,
        String brand
) {}
