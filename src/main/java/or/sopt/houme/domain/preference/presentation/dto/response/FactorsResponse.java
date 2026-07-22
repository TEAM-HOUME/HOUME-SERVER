package or.sopt.houme.domain.preference.presentation.dto.response;


import java.util.List;

public record FactorsResponse(
        List<FactorItem> factors
) {

    public record FactorItem(Long id, String text) {}
}
