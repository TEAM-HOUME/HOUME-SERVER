package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.repository.HouseFurnitureRepository;
import or.sopt.houme.domain.house.repository.HouseTasteRepository;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.generateImage.repository.ImageGenerationLogRepository;
import or.sopt.houme.domain.preference.repository.*;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.credit.repository.PaymentBtnClickLogRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRecommendBtnClickLogRepository;
import or.sopt.houme.domain.house.repository.InvalidHouseRequestRepository;
import or.sopt.houme.domain.address.repository.AddressRepository;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDeletionService {

    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final HouseFloorPlanRepository houseFloorPlanRepository;
    private final HouseFurnitureRepository houseFurnitureRepository;
    private final HouseTasteRepository houseTasteRepository;

    private final GenerateImageRepository generateImageRepository;
    private final ImageGenerationLogRepository imageGenerationLogRepository;

    private final GenerateImagePreferenceRepository generateImagePreferenceRepository;
    private final PromptPreferenceRepository promptPreferenceRepository;
    private final FactorRepository factorRepository;
    private final PreferenceRepository preferenceRepository;

    private final CarouselPreferenceRepository carouselPreferenceRepository;
    private final AddressRepository addressRepository;
    private final PaymentBtnClickLogRepository paymentBtnClickLogRepository;
    private final FurnitureRecommendBtnClickLogRepository furnitureRecommendBtnClickLogRepository;
    private final InvalidHouseRequestRepository invalidHouseRequestRepository;
    private final CreditRepository creditRepository;

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    public void delete(Long userId) {
        // 0. 현재 액세스 토큰 블랙리스트 처리 + 리프레시 토큰 제거 (best-effort)
        try {
            var attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                var request = attr.getRequest();
                String authHeader = request.getHeader(jwtConfig.getHeader());
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String accessToken = authHeader.substring(7).trim();
                    String jti = jwtUtil.getJti(accessToken);
                    long ttl = jwtUtil.getRemainingExpiration(accessToken);
                    if (jti != null && ttl > 0) {
                        blacklistTokenRepository.save(jti, ttl);
                    }
                }
            }
        } catch (Exception ignored) {}

        // 리프레시 토큰 제거
        try { refreshTokenRepository.deleteById(userId); } catch (Exception ignored) {}

        // 1. 캐러셀 선호도 및 연결된 Preference 삭제
        var carouselPrefs = carouselPreferenceRepository.findByUserId(userId);
        if (!carouselPrefs.isEmpty()) {
            var prefIds = carouselPrefs.stream().map(cp -> cp.getPreference().getId()).toList();
            carouselPreferenceRepository.deleteAllInBatch(carouselPrefs);
            prefIds.forEach(factorRepository::findPreferenceFactorAndDeleteByPreferenceId);
            preferenceRepository.deleteAllById(prefIds);
        }

        // 2. 하우스 관련 데이터 정리 (이미지, 매핑 등)
        var houses = houseRepository.findByUserId(userId);
        for (House house : houses) {
            // 이미지별 선호도/요인/Preference 삭제
            var images = generateImageRepository.findGenerateImagesByHouseId(house.getId());
            images.forEach(image -> {
                var gpList = generateImagePreferenceRepository.findAllByGenerateImageId(image.getId());
                if (!gpList.isEmpty()) {
                    var prefIds = gpList.stream().map(gp -> gp.getPreference().getId()).toList();
                    generateImagePreferenceRepository.deleteAll(gpList);
                    generateImagePreferenceRepository.flush();
                    prefIds.forEach(factorRepository::findPreferenceFactorAndDeleteByPreferenceId);
                    preferenceRepository.deleteAllById(prefIds);
                    preferenceRepository.flush();
                }
                long remain = generateImagePreferenceRepository.countByGenerateImageId(image.getId());
                if (remain > 0) {
                    var left = generateImagePreferenceRepository.findAllByGenerateImageId(image.getId());
                    generateImagePreferenceRepository.deleteAll(left);
                    generateImagePreferenceRepository.flush();
                }
            });
            if (!images.isEmpty()) {
                generateImageRepository.deleteAll(images);
                generateImageRepository.deleteByHouseId(house.getId());
                generateImageRepository.flush();
            }

            // 하우스에 연결된 PromptPreference(집 단위 선호도) 정리
            var ppList = promptPreferenceRepository.findAllByHouseId(house.getId());
            if (!ppList.isEmpty()) {
                var prefIds = ppList.stream().map(pp -> pp.getPreference().getId()).toList();
                promptPreferenceRepository.deleteAll(ppList);
                promptPreferenceRepository.flush();
                prefIds.forEach(factorRepository::findPreferenceFactorAndDeleteByPreferenceId);
                preferenceRepository.deleteAllById(prefIds);
                preferenceRepository.flush();
            }

            // 하우스 맵핑 데이터 삭제
            houseFloorPlanRepository.deleteByHouseId(house.getId());
            houseFloorPlanRepository.flush();
            houseFurnitureRepository.deleteByHouseId(house.getId());
            houseFurnitureRepository.flush();
            houseTasteRepository.deleteByHouseId(house.getId());
            houseTasteRepository.flush();
        }
        if (!houses.isEmpty()) {
            generateImageRepository.flush();
            promptPreferenceRepository.flush();
            houseFloorPlanRepository.flush();
            houseFurnitureRepository.flush();
            houseTasteRepository.flush();
            houseRepository.deleteAllInBatch(houses);
        }

        // 3. 유저 직접 참조 로그/데이터 삭제
        imageGenerationLogRepository.deleteByUserId(userId);
        invalidHouseRequestRepository.deleteByUserId(userId);
        addressRepository.deleteByUserId(userId);
        paymentBtnClickLogRepository.deleteByUserId(userId);
        furnitureRecommendBtnClickLogRepository.deleteByUserId(userId);
        creditRepository.deleteByUserId(userId);

        // 4. 마지막으로 유저 삭제
        userRepository.deleteById(userId);
    }
}

