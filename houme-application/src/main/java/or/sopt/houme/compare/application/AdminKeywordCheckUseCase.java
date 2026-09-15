package or.sopt.houme.compare.application;

import or.sopt.houme.compare.application.dto.KeywordCheckResponse;

public interface AdminKeywordCheckUseCase {
    KeywordCheckResponse check(String productName);
}
