package or.sopt.houme.global.legacy;

public record LegacyApiAlertCommand(
        String method,
        String documentedPath,
        String requestUri,
        String queryString,
        String reason,
        String traceId,
        String userId,
        String userAgent,
        String referer
) {
}
