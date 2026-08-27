package or.sopt.houme.legacyapi.application;

import or.sopt.houme.legacyapi.domain.LegacyApiCall;

/**
 * 삭제 후보 API 호출 이력 기록 유즈케이스.
 */
public interface LegacyApiCallHistoryService {

    void record(LegacyApiCall legacyApiCall);
}
