package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.presentation.controller.dto.ImageHistoriesResultPageResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageProfileResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface UserService {
    MyPageInfoResponse getMyPageInfo(User user);

    UserImageHistoryListResponse getUserImageHistoryList(User user);

    /**
     * 마이페이지 생성 이미지 이력 v2 응답을 조회합니다.
     */
    MyPageGeneratedImageV2Response getUserGeneratedImageHistoryListV2(User user);

    MyPageProfileResponse getMyPageProfile(User user);

    ImageHistoriesResultPageResponse getImageHistoryResultPage(User user, Long houseId);

    String updateUser(User user, String name, Gender gender, LocalDate birthday);

    String updateUserV2(User user, String nickname, Gender gender, LocalDate birthday);

    MyPageProfileResponse updateMyPageProfile(User user, String nickname, Gender gender, LocalDate birthday);

    // 사용자 이미지 생성 여부 저장
    @Transactional
    void updateHasGeneratedImage(User user);

}
