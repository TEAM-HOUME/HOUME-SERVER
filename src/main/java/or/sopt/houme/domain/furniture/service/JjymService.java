package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.response.JjymListResponse;

public interface JjymService {
    void jjymToggle(Long userId, Long recommendFurnitureId);

    JjymListResponse getMyJjyms(Long userId);
}
