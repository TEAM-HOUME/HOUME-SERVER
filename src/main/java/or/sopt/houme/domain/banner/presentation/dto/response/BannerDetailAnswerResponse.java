package or.sopt.houme.domain.banner.presentation.dto.response;

public record BannerDetailAnswerResponse(
        Long id,
        String text
) {

    public static BannerDetailAnswerResponse of(Long id, String text) {
        return new BannerDetailAnswerResponse(id, text);
    }
}
