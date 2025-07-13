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
import or.sopt.houme.domain.taste.repository.TagRepository;
import or.sopt.houme.domain.user.controller.dto.*;
import or.sopt.houme.domain.user.entity.Gender;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.CredentialException;
import java.time.LocalDate;
import java.util.List;

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
        validateImageHistoryExists(findUser);  // 생성된 이미지 이력이 없으면 예외터짐
        List<UserImageHistoryDTO> histories = userRepository.getUserImageHistory(findUser.getId());
        return UserImageHistoryListResponse.of(histories);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageHistoryResultPageResponse getImageHistoryResultPage(User user, Long imageId) {
        User findUser = findUser(user);
        House house = houseRepository.findHouseByUserIdAndImageId(findUser.getId(), imageId).orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE_ENTITY));
        Tag tag = tagRepository.findTagByUserIdAndImageId(findUser.getId(), imageId).orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));
        GenerateImage generateImage = generateImageRepository.findGenerateImageByUserIdAndImageId(findUser.getId(), imageId).orElseThrow(() -> new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY));

        return ImageHistoryResultPageResponse.of(house.getEquilibrium().toString(), house.getForm().toString(), tag.getTagName(), findUser.getName(), generateImage.getUrl());
    }

    @Override
    public void updateUser(User user, String name, Gender gender, LocalDate birthday) {

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
    }

    private User findUser(User user) {
        return userRepository.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateImageHistoryExists(User user) {
        userRepository.findImageHistoryById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.IMAGE_HISTORY_NOT_FOUND));
    }
}
