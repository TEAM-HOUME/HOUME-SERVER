package or.sopt.houme.domain.explore.presentation.dto.response;

import java.util.List;

public record BannerDetailResponse(
        String bannerName,
        String bannerImageUrl,
        String question,
        List<BannerDetailAnswerResponse> answers
) {

    public static BannerDetailResponse of(
            String bannerName,
            String bannerImageUrl,
            String question,
            List<BannerDetailAnswerResponse> answers
    ) {
        return new BannerDetailResponse(bannerName, bannerImageUrl, question, answers);
    }
}
