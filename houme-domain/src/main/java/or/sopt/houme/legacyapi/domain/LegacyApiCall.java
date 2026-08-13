package or.sopt.houme.legacyapi.domain;

/**
 * 삭제 후보 API의 호출 이력이다.
 */
public record LegacyApiCall(
        String method,
        String requestUri,
        String apiPath,
        Long userId,
        String traceId
) {
}
