package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendFurnitureServiceImpl implements RecommendFurnitureService {

    private final RecommendFurnitureRepository recommendFurnitureRepository;


    @Override
    public Map<Long, Long> saveRecommendFurniture(
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto,
            CurationSource source
    ) {
        return saveSingleRecommendFurniture(requestDto, source);
    }


    /**
     * 단일 추천 가구를 저장하는 헬퍼 메서드입니다.
     *
     * 리스트 형식으로 input을 받아 가구들을 저장합니다
     * */
    private Map<Long, Long> saveSingleRecommendFurniture(
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto,
            CurationSource source
    ) {
        Map<Long, Long> idMapByProductId = new HashMap<>();

        for (FurnitureProductsInfoResponse.FurnitureProductInfo furnitureProductInfo : requestDto) {
            Long productId = furnitureProductInfo.furnitureProductId();

            RecommendFurniture entity;

            boolean exists = recommendFurnitureRepository.existsBySourceAndFurnitureProductId(source, productId);
            if (exists) {
                entity = recommendFurnitureRepository.findBySourceAndFurnitureProductId(source, productId)
                        .orElseThrow();
            } else {
                entity = RecommendFurniture.from(
                        furnitureProductInfo.furnitureProductImageUrl(),
                        furnitureProductInfo.furnitureProductSiteUrl(),
                        furnitureProductInfo.furnitureProductName(),
                        furnitureProductInfo.furnitureProductMallName(),
                        productId,
                        source
                );
                entity = recommendFurnitureRepository.save(entity);
            }

            idMapByProductId.put(productId, entity.getId());
        }

        return idMapByProductId;
    }

}
