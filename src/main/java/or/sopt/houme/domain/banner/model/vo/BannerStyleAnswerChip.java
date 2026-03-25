package or.sopt.houme.domain.banner.model.vo;

public record BannerStyleAnswerChip(
        Long id,
        Integer order,
        String label,
        String selectedPrompt,
        Long curationRawProductId
) {
}
