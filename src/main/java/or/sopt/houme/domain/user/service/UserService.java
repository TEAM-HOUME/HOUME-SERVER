package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.presentation.controller.dto.ImageHistoriesResultPageResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface UserService {
    MyPageInfoResponse getMyPageInfo(User user);

    UserImageHistoryListResponse getUserImageHistoryList(User user);

    ImageHistoriesResultPageResponse getImageHistoryResultPage(User user, Long houseId);

    String updateUser(User user, String nickname, Gender gender, LocalDate birthday);

    // 사용자 이미지 생성 여부 저장
    @Transactional
    void updateHasGeneratedImage(User user);

}
