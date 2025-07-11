package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.controller.dto.CreateUserRequest;
import or.sopt.houme.domain.user.controller.dto.ImageHistoryResultPageResponse;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.entity.User;

public interface UserService {
    MyPageInfoResponse getMyPageInfo(User user);

    UserImageHistoryListResponse getUserImageHistoryList(User user);

    ImageHistoryResultPageResponse getImageHistoryResultPage(User user, Long imageId);

    void updateUser(User user, CreateUserRequest createUserRequest);
}
