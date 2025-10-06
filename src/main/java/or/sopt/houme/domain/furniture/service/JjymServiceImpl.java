package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.entity.Jjym;
import or.sopt.houme.domain.furniture.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.dto.response.JjymItemResponse;
import or.sopt.houme.domain.furniture.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JjymServiceImpl implements JjymService {

    private final JjymRepository jjymRepository;
    private final UserRepository userRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;

    @Override
    public void jjymToggle(Long userId, Long recommendFurnitureId) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        RecommendFurniture furniture = recommendFurnitureRepository.findById(recommendFurnitureId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Optional<Jjym> existing = jjymRepository.findByUserIdAndRecommendFurnitureId(user.getId(), furniture.getId());

        if (existing.isPresent()) {
            // 이미 찜되어 있으면 찜을 해제합니다
            jjymRepository.delete(existing.get());
        } else {
            // 찜이 되어 있지 않으면 찜을 해제합니다
            Jjym jjym = Jjym.of(user, furniture);
            jjymRepository.save(jjym);
        }


    }

    @Transactional(readOnly = true)
    @Override
    public JjymListResponse getMyJjyms(Long userId) {

        List<Jjym> jjyms = jjymRepository.findAllByUserIdWithFurnitureOrderByCreatedAtDesc(userId);

        List<JjymItemResponse> items = jjyms.stream()
                .map(j -> JjymItemResponse.from(j.getRecommendFurniture()))
                .collect(Collectors.toList());

        return JjymListResponse.of(items);

    }
}
