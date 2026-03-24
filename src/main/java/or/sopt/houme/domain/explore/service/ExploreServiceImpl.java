package or.sopt.houme.domain.explore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailAnswerResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExploreServiceImpl implements ExploreService {

    private static final TypeReference<List<BannerStyleAnswerChip>> STYLE_ANSWER_CHIP_TYPE = new TypeReference<>() {};

    private final BannerRepository bannerRepository;
    private final ObjectMapper objectMapper;

    @Override
    public BannerExploreListResponse getExploreBanners(Long bannerId) {
        List<Banner> banners = bannerRepository.findAllWithRawProducts(BannerType.BANNER, false).stream()
                .sorted((left, right) -> Long.compare(left.getId(), right.getId()))
                .toList();
        int startIndex = findBannerStartIndex(banners, bannerId);

        List<BannerExploreResponse> orderedBanners = new ArrayList<>(banners.size());
        for (int index = 0; index < banners.size(); index++) {
            Banner banner = banners.get((startIndex + index) % banners.size());
            orderedBanners.add(BannerExploreResponse.from(banner));
        }
        return BannerExploreListResponse.of(orderedBanners);
    }

    @Override
    public BannerDetailResponse getExploreBannerDetail(Long bannerId) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId, BannerType.BANNER, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));

        return BannerDetailResponse.of(
                banner.getBannerTitle(),
                banner.getBannerImageUrl(),
                banner.getStyleQuestion(),
                parseStyleAnswerChips(banner.getStyleAnswerChipsJson()).stream()
                        .map(chip -> BannerDetailAnswerResponse.of(chip.label()))
                        .toList()
        );
    }

    private int findBannerStartIndex(List<Banner> banners, Long bannerId) {
        for (int index = 0; index < banners.size(); index++) {
            if (banners.get(index).getId().equals(bannerId)) {
                return index;
            }
        }
        throw new GeneralException(ErrorCode.NOT_FOUND_BANNER);
    }

    private List<BannerStyleAnswerChip> parseStyleAnswerChips(String styleAnswerChipsJson) {
        if (styleAnswerChipsJson == null || styleAnswerChipsJson.isBlank()) {
            return List.of();
        }
        try {
            List<BannerStyleAnswerChip> chips = objectMapper.readValue(styleAnswerChipsJson, STYLE_ANSWER_CHIP_TYPE);
            if (chips == null) {
                return List.of();
            }
            return chips.stream()
                    .sorted((left, right) -> Integer.compare(left.order(), right.order()))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }
}
