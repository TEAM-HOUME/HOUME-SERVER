package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;

import java.util.List;

/** 수집(soozip) 크롤링 인바운드 계약 (#582 — 구현은 jsoup 크롤러라 infra 배정). */
public interface SoozipCrawlingService {

    List<NaverFurnitureProductDto> fetchCategoryProducts(int cateNo, Integer maxPages);
}
