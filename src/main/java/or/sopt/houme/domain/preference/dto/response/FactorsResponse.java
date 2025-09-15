package or.sopt.houme.domain.preference.dto.response;

import or.sopt.houme.domain.preference.entity.Factor;

import java.util.List;
import java.util.stream.Collectors;

public record FactorsResponse(
        List<String> factorTexts
) {
    public static FactorsResponse from(List<Factor> entities) {
        List<String> texts = entities.stream()
                .map(Factor::getFactorText)   // 엔티티 필드명 맞게 수정
                .collect(Collectors.toList());
        return new FactorsResponse(texts);
    }
}
