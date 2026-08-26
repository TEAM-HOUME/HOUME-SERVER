package or.sopt.houme.domain.coupang.service;

/** 쿠팡 상품 수집 배치를 한 번 실행하는 인바운드 계약입니다. */
public interface CoupangCollectionBatchService {

    void runOneJob();
}
