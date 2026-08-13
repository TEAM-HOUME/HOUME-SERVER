package or.sopt.houme.legacyapi.domain.port.out;

import or.sopt.houme.legacyapi.domain.LegacyApiCall;

/**
 * 삭제 후보 API 호출 이력 저장 포트.
 */
public interface LegacyApiCallHistoryPort {

    void save(LegacyApiCall legacyApiCall);
}
