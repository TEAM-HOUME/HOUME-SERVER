package or.sopt.houme.domain.banner.presentation.dto.response;


public record LandingResponse(
        Long id,
        Long bannerId,
        String name,
        String imageUrl
) {
}
