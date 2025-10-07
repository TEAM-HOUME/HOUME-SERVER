package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.generateImage.repository.ImageGenerationLogRepository;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.repository.HouseFurnitureRepository;
import or.sopt.houme.domain.house.repository.HouseTasteRepository;
import or.sopt.houme.domain.preference.entity.*;
import or.sopt.houme.domain.preference.repository.*;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.domain.user.controller.dto.ImageHistoriesResultPageResponse;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.entity.Gender;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.address.repository.AddressRepository;
import or.sopt.houme.domain.preference.repository.CarouselPreferenceRepository;
import or.sopt.houme.domain.credit.repository.PaymentBtnClickLogRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRecommendBtnClickLogRepository;
import or.sopt.houme.domain.house.repository.InvalidHouseRequestRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.*;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final HouseFloorPlanRepository houseFloorPlanRepository;
    private final HouseFurnitureRepository houseFurnitureRepository;
    private final HouseTasteRepository houseTasteRepository;
    private final TagRepository tagRepository;
    private final GenerateImageRepository generateImageRepository;
    private final ImageGenerationLogRepository imageGenerationLogRepository;
    private final CreditRepository creditRepository;
    private final GenerateImagePreferenceRepository generateImagePreferenceRepository;
    private final PromptPreferenceRepository promptPreferenceRepository;
    private final FactorRepository factorRepository;
    private final PreferenceRepository preferenceRepository;
    private final PreferenceFactorRepository preferenceFactorRepository;
    private final AddressRepository addressRepository;
    private final CarouselPreferenceRepository carouselPreferenceRepository;
    private final PaymentBtnClickLogRepository paymentBtnClickLogRepository;
    private final FurnitureRecommendBtnClickLogRepository furnitureRecommendBtnClickLogRepository;
    private final InvalidHouseRequestRepository invalidHouseRequestRepository;

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    @Override
    @Transactional(readOnly = true)
    public MyPageInfoResponse getMyPageInfo(User user) {
        User findUser = findUser(user);
        String name = findUser.getName();
        Long creditCount = userRepository.countByMemberIdAndStatus(findUser.getId());
        return MyPageInfoResponse.of(name, creditCount);
    }

    @Override
    @Transactional(readOnly = true)
    public UserImageHistoryListResponse getUserImageHistoryList(User user) {
        User findUser = findUser(user);

        // 1. 유저가 생성한 House 목록 조회 (isValid == true)
        List<House> houses = houseRepository.findValidHouseByUserId(findUser.getId());

        List<UserImageHistoryDTO> histories = new ArrayList<>();

        for (House house : houses) {
            // 2. 각 house에 연결된 이미지가 없으면 skip
            Optional<GenerateImage> generateImage = generateImageRepository.findByHouseId(house.getId());
            if (generateImage.isEmpty()) continue;

            // 3. 해당 house에서 가장 많이 등장한 태그 가져오기
            Optional<Tag> representativeTag = tagRepository.findMostFrequentTagByHouseId(house.getId());
            if (representativeTag.isEmpty()) continue;

            // 4. DTO 생성
            UserImageHistoryDTO dto = new UserImageHistoryDTO(
                    generateImage.get().getId(),
                    generateImage.get().getUrl(),
                    representativeTag.get().getTagNameKr(),
                    house.getEquilibrium(),
                    house.getForm()
            );
            histories.add(dto);
        }

        return UserImageHistoryListResponse.of(histories);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageHistoriesResultPageResponse getImageHistoryResultPage(User user, Long imageId) {
        User findUser = findUser(user);

        // 1. house, tag 조회
        House house = houseRepository.findHouseByUserIdAndImageId(findUser.getId(), imageId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));
        Optional<Preference> preferenceByUserIdAndImageId = preferenceRepository.findPreferenceByUserIdAndImageId(findUser.getId(), imageId);

        // 2. houseId 에 해당하는 generateImage 리스트 조회
        List<GenerateImage> generateImages = generateImageRepository.findGenerateImagesByHouseId(house.getId());
        if (generateImages.isEmpty()) {
            throw new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY);
        }

        List<Boolean> likes = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();
        // 선택했던 factor 조회
        List<Factor> factors = new ArrayList<>();

        // 좋아요 객체
        Optional<Preference> preference;

        // 3. 최신 GenerateImagePreference 조회 (선호 여부)
        for (GenerateImage generateImage : generateImages) {
            Optional<GenerateImagePreference> optionalGenerateImagePreference =
                    generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage.getId());

            if (optionalGenerateImagePreference.isPresent()){
                likes.add(optionalGenerateImagePreference.get().getPreference().isLike());
            } else {
                likes.add(null);
            }

            tags.add(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId())
                    .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY)));

            // Preference 찾기
            preference = preferenceRepository.findPreferenceByUserIdAndImageId(findUser.getId(), generateImage.getId());

            // Factor 관련
            if (preference.isPresent()){
                PreferenceFactor preferenceFactor = preferenceFactorRepository.findByPreference(preferenceByUserIdAndImageId.get())
                        .orElse(null);

                if (preferenceFactor != null){
                    factors.add(factorRepository.findById(preferenceFactor.getFactor().getId())
                            .orElse(null));
                } else {
                    factors.add(null);
                }
            } else {
                factors.add(null);
            }
        }

        // 4. GenerateImage 리스트와 likes 리스트를 함께 사용하여 DTO 변환
        List<ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse> histories =
                IntStream.range(0, generateImages.size()) // 인덱스를 활용하여 스트림 생성
                        .mapToObj(i -> {
                            GenerateImage generateImage = generateImages.get(i);
                            Boolean isLike = likes.get(i); // likes 리스트에서 해당 인덱스의 값 가져오기
                            Tag tag = tags.get(i);
                            Factor factor = factors.get(i);

                            return ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse.of(
                                    house.getEquilibrium().getDescription(),
                                    house.getForm().toString(),
                                    tag.getTagNameKr(),
                                    findUser.getName(),
                                    generateImage.getUrl(),
                                    isLike,
                                    factor == null ? null : factor.getId(),
                                    factor == null ? null : factor.getFactorText()
                            );
                        })
                        .toList();

        // 5. 응답 DTO 생성
        return ImageHistoriesResultPageResponse.of(histories);
    }

    @Override
    public String updateUser(User user, String name, Gender gender, LocalDate birthday) {

        User findUser = findUser(user);
        findUser.updateUserFromSignUp(name, birthday, gender);

        try {
            Credit newCredit = Credit.builder()
                    .status(CreditStatus.ACTIVE)
                    .user(findUser)
                    .build();

            creditRepository.save(newCredit);
        }catch (Exception e) {
            throw new CreditException(ErrorCode.CREDIT_CREATE_EXCEPTION);
        }

        return findUser.getName();
    }

    // 이미지 생성 이력 저장
    @Transactional
    @Override
    public void updateHasGeneratedImage(User user) {
        user.updateHasGeneratedImage();

        userRepository.save(user);
    }

    @Override
    public void delete(Long userId){
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
            var prefIds = carouselPrefs.stream()
                    .map(cp -> cp.getPreference().getId())
                    .toList();
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
                    // 매핑을 개별 삭제(배치 삭제로 인한 flush 순서 이슈 회피)
                    generateImagePreferenceRepository.deleteAll(gpList);
                    generateImagePreferenceRepository.flush();
                    // 요인/선호 삭제
                    prefIds.forEach(factorRepository::findPreferenceFactorAndDeleteByPreferenceId);
                    preferenceRepository.deleteAllById(prefIds);
                    preferenceRepository.flush();
                }

                // 안전장치: 남아있는 매핑이 있다면 한 번 더 제거 시도
                long remain = generateImagePreferenceRepository.countByGenerateImageId(image.getId());
                if (remain > 0) {
                    var left = generateImagePreferenceRepository.findAllByGenerateImageId(image.getId());
                    generateImagePreferenceRepository.deleteAll(left);
                    generateImagePreferenceRepository.flush();
                }
            });
            if (!images.isEmpty()) {
                // 배치 삭제 대신 순차 삭제로 FK 안정성 확보
                generateImageRepository.deleteAll(images);
                // 혹시 남아있을 수 있는 이미지 일괄 삭제 (안전망)
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
            // 이미지 삭제가 DB에 반영된 이후 하우스 삭제
            // 매핑/이미지 삭제가 확실히 반영되도록 마지막 플러시
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

    private User findUser(User user) {
        return userRepository.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

}
