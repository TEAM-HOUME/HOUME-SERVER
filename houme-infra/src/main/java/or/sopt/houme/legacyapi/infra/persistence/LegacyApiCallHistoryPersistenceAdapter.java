package or.sopt.houme.legacyapi.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.legacyapi.domain.LegacyApiCall;
import or.sopt.houme.legacyapi.domain.port.out.LegacyApiCallHistoryPort;
import org.springframework.stereotype.Component;

/** {@link LegacyApiCallHistoryPort}의 JPA 구현 어댑터. */
@Component
@RequiredArgsConstructor
public class LegacyApiCallHistoryPersistenceAdapter implements LegacyApiCallHistoryPort {

    private final LegacyApiCallHistoryJpaRepository legacyApiCallHistoryJpaRepository;

    @Override
    public void save(LegacyApiCall legacyApiCall) {
        legacyApiCallHistoryJpaRepository.save(LegacyApiCallHistoryJpaEntity.builder()
                .method(legacyApiCall.method())
                .requestUri(legacyApiCall.requestUri())
                .userId(legacyApiCall.userId())
                .build());
    }
}
