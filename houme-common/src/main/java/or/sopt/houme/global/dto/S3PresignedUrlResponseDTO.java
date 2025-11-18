package or.sopt.houme.global.dto;

public record S3PresignedUrlResponseDTO (
        String uploadUrl,
        String publicUrl,
        String keyName,
        String directory
) {}
