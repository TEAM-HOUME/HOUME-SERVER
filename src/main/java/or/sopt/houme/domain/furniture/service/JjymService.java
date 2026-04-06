package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;

public interface JjymService {
    boolean jjymToggle(Long userId, Long recommendFurnitureId);

    boolean rawProductJjymToggle(Long userId, Long rawProductId);

    void likeRawProduct(Long userId, Long rawProductId);

    JjymListResponse getMyJjyms(Long userId);

    JjymV2ListResponse getMyRawProductJjyms(Long userId);
}
