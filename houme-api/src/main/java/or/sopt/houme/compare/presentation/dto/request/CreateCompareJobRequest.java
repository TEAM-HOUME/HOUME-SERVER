package or.sopt.houme.compare.presentation.dto.request;

public record CreateCompareJobRequest(
        String sourceUrl,
        DummyProductInput dummyProduct
) {}
