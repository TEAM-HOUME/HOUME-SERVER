package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.domain.user.controller.dto.*;
import or.sopt.houme.domain.user.entity.Gender;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final TagRepository tagRepository;
    private final GenerateImageRepository generateImageRepository;
    private final CreditRepository creditRepository;

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
    public ImageHistoryResultPageResponse getImageHistoryResultPage(User user, Long imageId) {
        User findUser = findUser(user);
        House house = houseRepository.findHouseByUserIdAndImageId(findUser.getId(), imageId).orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));
        Tag tag = tagRepository.findTagByUserIdAndImageId(findUser.getId(), imageId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));
        GenerateImage generateImage = generateImageRepository.findGenerateImageByUserIdAndImageId(findUser.getId(), imageId).orElseThrow(() -> new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY));

        return ImageHistoryResultPageResponse.of(house.getEquilibrium().getDescription(), house.getForm().toString(), tag.getTagNameKr(), findUser.getName(), generateImage.getUrl());
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

    private User findUser(User user) {
        return userRepository.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }
}
