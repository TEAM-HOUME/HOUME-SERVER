package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;

import java.util.List;

public interface JjymService {
    boolean jjymToggle(Long userId, Long recommendFurnitureId);

    boolean rawProductJjymToggle(Long userId, Long rawProductId);

    void likeRawProduct(Long userId, Long rawProductId);

    void hateRawProduct(Long userId, Long rawProductId);

    List<Long> getLikedRawProductProductIds(Long userId);

    JjymListResponse getMyJjyms(Long userId);

    JjymV2ListResponse getMyRawProductJjyms(Long userId);
}
