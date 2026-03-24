package or.sopt.houme.domain.explore.presentation.dto.response;

public record BannerDetailAnswerResponse(
        String text
) {

    public static BannerDetailAnswerResponse of(String text) {
        return new BannerDetailAnswerResponse(text);
    }
}
