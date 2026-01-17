package or.sopt.houme.domain.preference.presentation.dto.response;

import or.sopt.houme.domain.preference.model.entity.Factor;

import java.util.List;

public record FactorsResponse(
        List<FactorItem> factors
) {
    public static FactorsResponse from(List<Factor> entities) {
        return new FactorsResponse(
                entities.stream()
                        .map(f -> new FactorItem(f.getId(), f.getFactorText()))
                        .toList()
        );
    }

    public record FactorItem(Long id, String text) {}
}
