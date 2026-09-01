package or.sopt.houme.legacyapi.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.legacyapi.domain.LegacyApiCall;
import or.sopt.houme.legacyapi.domain.port.out.LegacyApiCallHistoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegacyApiCallHistoryServiceImpl implements LegacyApiCallHistoryService {

    private final LegacyApiCallHistoryPort legacyApiCallHistoryPort;

    @Override
    @Transactional
    public void record(LegacyApiCall legacyApiCall) {
        legacyApiCallHistoryPort.save(legacyApiCall);
    }
}
