package or.sopt.houme.domain.banner.model.vo;

public record BannerStyleAnswerChip(
        Integer order,
        String label,
        String selectedPrompt,
        Long curationRawProductId
) {
}
