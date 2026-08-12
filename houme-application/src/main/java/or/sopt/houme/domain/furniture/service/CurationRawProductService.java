package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.service.dto.CurationRawProductSaveResult;
import or.sopt.houme.furniture.domain.FurnitureTagView;

import java.util.List;

/** 원본상품 수집/후보 조회 인바운드 계약 (#582 — 구현은 엔티티 저장 로직이라 infra 배정). */
public interface CurationRawProductService {

    List<NaverFurnitureProductDto> getCandidatesByFurnitureTag(FurnitureTagView furnitureTag);

    CurationRawProductSaveResult saveAll(String source, SoozipCategory category, List<NaverFurnitureProductDto> products);
}
