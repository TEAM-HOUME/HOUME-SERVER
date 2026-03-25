package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response;

public record AdminBannerStyleAnswerChipResponse(
        Long id,
        Integer order,
        String label,
        String selectedPrompt,
        Long curationRawProductId,
        String curationRawProductName,
        String curationRawProductImageUrl
) {
}
