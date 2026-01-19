package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.service.CurationFurnitureService;
import or.sopt.houme.domain.furniture.service.ImageHashService;
import or.sopt.houme.domain.furniture.service.NaverShopService;
import or.sopt.houme.global.discord.DiscordWebhookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
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
    private final DiscordWebhookService discordWebhookService;

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void refreshCurationResults() {
        Instant startedAt = Instant.now();
        int totalTags = 0;
        int successTags = 0;
        int emptyTags = 0;
        String status = "성공";
        Exception failure = null;

        try {
            List<FurnitureTag> furnitureTags = furnitureTagRepository.findAll();
            totalTags = furnitureTags.size();
            if (furnitureTags.isEmpty()) {
                status = "대상없음";
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
                    emptyTags++;
                    log.warn("큐레이션 배치: 결과 없음 tagId={}", furnitureTag.getId());
                    continue;
                }

                curationFurnitureService.saveCurationResults(furnitureTag, infos);
                successTags++;
            }
        } catch (Exception e) {
            status = "실패";
            failure = e;
            throw e;
        } finally {
            StringBuilder message = new StringBuilder();
            long elapsedSeconds = Duration.between(startedAt, Instant.now()).toSeconds();
            message.append("[큐레이션 배치] 상태=").append(status)
                    .append(" 대상=").append(totalTags)
                    .append(" 성공=").append(successTags)
                    .append(" 결과없음=").append(emptyTags)
                    .append(" 소요=").append(elapsedSeconds).append("s");
            if (failure != null) {
                message.append(" 에러=").append(failure.getClass().getSimpleName());
                String errorMessage = failure.getMessage();
                if (errorMessage != null && !errorMessage.isBlank()) {
                    message.append(" (").append(errorMessage).append(")");
                }
            }
            discordWebhookService.sendMessage(message.toString());
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
