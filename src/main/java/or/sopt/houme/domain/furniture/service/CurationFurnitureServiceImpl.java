package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.repository.CurationFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurationFurnitureServiceImpl implements CurationFurnitureService {

    private final CurationFurnitureRepository curationFurnitureRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final RecommendFurnitureService recommendFurnitureService;

    @Transactional(readOnly = true)
    @Override
    public List<FurnitureProductsInfoResponse.FurnitureProductInfo> getCurationProducts(
            FurnitureTag furnitureTag,
            CurationSource source
    ) {
        List<CurationFurniture> curations =
                curationFurnitureRepository.findAllByFurnitureTagAndSourceOrderByRankAsc(furnitureTag, source);
        if (curations.isEmpty()) {
            return List.of();
        }

        return curations.stream()
                .map(curation -> {
                    RecommendFurniture recommendFurniture = curation.getRecommendFurniture();

                    return FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                            recommendFurniture.getId(),
                            recommendFurniture.getFurnitureProductImageUrl(),
                            recommendFurniture.getFurnitureProductSiteUrl(),
                            recommendFurniture.getFurnitureProductName(),
                            recommendFurniture.getFurnitureProductMallName(),
                            recommendFurniture.getFurnitureProductId(),
                            curation.getSimilarity()
                    );
                })
                .toList();
    }

    @Transactional
    @Override
    public List<FurnitureProductsInfoResponse.FurnitureProductInfo> saveCurationResults(
            FurnitureTag furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos,
            CurationSource source
    ) {
        if (infos == null || infos.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> idMapByProductId = recommendFurnitureService.saveRecommendFurniture(infos, source);
        LocalDateTime fetchedAt = LocalDateTime.now();
        List<CurationFurniture> curations = new ArrayList<>();

        int rank = 1;
        for (FurnitureProductsInfoResponse.FurnitureProductInfo info : infos) {
            Long recommendFurnitureId = idMapByProductId.get(info.furnitureProductId());
            if (recommendFurnitureId == null) {
                log.warn("큐레이션 저장 실패: recommendFurnitureId 없음, productId={}", info.furnitureProductId());
                continue;
            }

            RecommendFurniture recommendFurniture = recommendFurnitureRepository.getReferenceById(recommendFurnitureId);
            curations.add(CurationFurniture.of(
                    furnitureTag,
                    recommendFurniture,
                    rank,
                    source,
                    info.similarity(),
                    fetchedAt
            ));
            rank++;
        }

        if (curations.isEmpty()) {
            return List.of();
        }

        curationFurnitureRepository.deleteByFurnitureTagAndSource(furnitureTag, source);
        curationFurnitureRepository.saveAll(curations);

        return mapResponseWithIds(infos, idMapByProductId);
    }

    private List<FurnitureProductsInfoResponse.FurnitureProductInfo> mapResponseWithIds(
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos,
            Map<Long, Long> idMapByProductId
    ) {
        return infos.stream()
                .map(info -> FurnitureProductsInfoResponse.FurnitureProductInfo.of(
                        idMapByProductId.get(info.furnitureProductId()),
                        info.furnitureProductImageUrl(),
                        info.furnitureProductSiteUrl(),
                        info.furnitureProductName(),
                        info.furnitureProductMallName(),
                        info.furnitureProductId(),
                        info.similarity()
                ))
                .toList();
    }
}
