package or.sopt.houme.domain.furniture.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.service.CurationFurnitureService;
import or.sopt.houme.domain.furniture.service.ImageHashService;
import or.sopt.houme.domain.furniture.service.NaverShopService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurationFurnitureScheduler {

    private static final int NAVER_DISPLAY = 50;
    private static final int TOP_N = 5;
    private static final int MAX_RETRY = 2;
    private static final long RETRY_BACKOFF_MILLIS = 1_000L;
    private static final long NAVER_RATE_LIMIT_MILLIS = 200L;

    private final FurnitureTagRepository furnitureTagRepository;
    private final NaverShopService naverShopService;
    private final ImageHashService imageHashService;
    private final CurationFurnitureService curationFurnitureService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void refreshCurationResults() {
        List<FurnitureTag> furnitureTags = furnitureTagRepository.findAll();
        if (furnitureTags.isEmpty()) {
            log.info("큐레이션 배치: 대상 태그 없음");
            return;
        }

        for (int i = 0; i < furnitureTags.size(); i++) {
            if (i > 0) {
                sleep(NAVER_RATE_LIMIT_MILLIS);
            }

            FurnitureTag furnitureTag = furnitureTags.get(i);
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos = fetchCurationWithRetry(furnitureTag);
            if (infos.isEmpty()) {
                log.warn("큐레이션 배치: 결과 없음 tagId={}", furnitureTag.getId());
                continue;
            }

            curationFurnitureService.saveCurationResults(furnitureTag, infos);
        }
    }

    private List<FurnitureProductsInfoResponse.FurnitureProductInfo> fetchCurationWithRetry(FurnitureTag furnitureTag) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return fetchCurationInfos(furnitureTag);
            } catch (Exception e) {
                log.warn("큐레이션 배치 실패: tagId={}, attempt={}", furnitureTag.getId(), attempt, e);
                sleep(RETRY_BACKOFF_MILLIS * attempt);
            }
        }

        return List.of();
    }

    private List<FurnitureProductsInfoResponse.FurnitureProductInfo> fetchCurationInfos(FurnitureTag furnitureTag) {
        List<NaverFurnitureProductDto> products = naverShopService.search(furnitureTag.getSearchKeyword(), NAVER_DISPLAY);
        return imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), products, TOP_N);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
