package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.KeywordCheckResponse;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminKeywordCheckService implements AdminKeywordCheckUseCase {

    private final KeywordTranslationPort keywordTranslationPort;

    @Override
    public KeywordCheckResponse check(String productName) {
        return KeywordCheckResponse.from(keywordTranslationPort.translateToBoth(productName));
    }
}
