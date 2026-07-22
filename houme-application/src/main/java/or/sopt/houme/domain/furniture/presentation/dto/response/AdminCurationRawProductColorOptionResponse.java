package or.sopt.houme.domain.furniture.presentation.dto.response;

public record AdminCurationRawProductColorOptionResponse(
        String label,
        String value
) {
    public static AdminCurationRawProductColorOptionResponse of(String label, String value) {
        return new AdminCurationRawProductColorOptionResponse(label, value);
    }
}
