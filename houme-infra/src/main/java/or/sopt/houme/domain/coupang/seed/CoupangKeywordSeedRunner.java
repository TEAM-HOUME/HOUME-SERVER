package or.sopt.houme.domain.coupang.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.coupang.service.CoupangCollectionJobService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 애플리케이션 시작 시 초기 키워드와 수집 Job을 멱등하게 준비합니다. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangKeywordSeedRunner implements ApplicationRunner {

    private final CoupangCollectionJobService collectionJobService;

    @Override
    public void run(ApplicationArguments args) {
        collectionJobService.seedKeywords(CoupangKeywordCatalog.defaults());
        log.info("쿠팡 배치 기본 키워드 시딩을 확인했습니다. keywordCount={}", CoupangKeywordCatalog.defaults().size());
    }
}
