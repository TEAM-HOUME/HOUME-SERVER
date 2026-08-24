package or.sopt.houme.coupang.service;

import or.sopt.houme.coupang.domain.CoupangProductSearchResult;

import java.util.List;

/** 관리자 검증용 쿠팡 파트너스 단건 상품 검색 유즈케이스입니다. */
public interface CoupangProductSearchService {

    List<CoupangProductSearchResult> search(String keyword, int limit);
}
