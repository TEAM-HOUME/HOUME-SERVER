package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendFurnitureServiceImpl implements RecommendFurnitureService {

    private final RecommendFurnitureRepository recommendFurnitureRepository;


    @Override
    public void saveRecommendFurniture(List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto) {

        saveSingleRecommendFurniture(requestDto);
    }


    /**
     * 단일 추천 가구를 저장하는 헬퍼 메서드입니다.
     *
     * 리스트 형식으로 input을 받아 가구들을 저장합니다
     * */
    private void saveSingleRecommendFurniture(List<FurnitureProductsInfoResponse.FurnitureProductInfo> requestDto) {

        for (FurnitureProductsInfoResponse.FurnitureProductInfo furnitureProductInfo : requestDto) {

            boolean existsByFurnitureProductId = recommendFurnitureRepository.existsByFurnitureProductId(furnitureProductInfo.furnitureProductId());

            if (existsByFurnitureProductId) {
                continue;
            }

            RecommendFurniture from = RecommendFurniture.from(
                    furnitureProductInfo.furnitureProductImageUrl(),
                    furnitureProductInfo.furnitureProductSiteUrl(),
                    furnitureProductInfo.furnitureProductName(),
                    furnitureProductInfo.furnitureProductMallName(),
                    furnitureProductInfo.furnitureProductId()
            );

            recommendFurnitureRepository.save(from);
        }

    }

}
