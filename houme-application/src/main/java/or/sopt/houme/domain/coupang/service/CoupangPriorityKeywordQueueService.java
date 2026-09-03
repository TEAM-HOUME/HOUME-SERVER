package or.sopt.houme.domain.coupang.service;

/** 사용자 가격비교 요청에서 추출한 쿠팡 검색어를 우선 수집 큐에 등록하는 인바운드 계약입니다. */
public interface CoupangPriorityKeywordQueueService {

    void enqueueIfAbsent(String keyword, Long furnitureId);
}
