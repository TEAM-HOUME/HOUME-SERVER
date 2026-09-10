package or.sopt.houme.compare.application.dto;

import or.sopt.houme.compare.domain.KeywordPair;

public record KeywordCheckResponse(String english, String korean) {
    public static KeywordCheckResponse from(KeywordPair pair) {
        return new KeywordCheckResponse(pair.english(), pair.korean());
    }
}
