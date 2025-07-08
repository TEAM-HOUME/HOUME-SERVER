package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyPageInfoResponse getMyPageInfo(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        String name = findUser.getName();
        long creditCount = userRepository.countByMemberIdAndStatus(user.getId());
        return MyPageInfoResponse.of(name, creditCount);
    }

    @Transactional(readOnly = true)
    public UserImageHistoryListResponse getUserImageHistoryList(User user) {
        User findUser = userRepository.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        List<UserImageHistoryDTO> histories = userRepository.getUserImageHistory(findUser.getId());
        return UserImageHistoryListResponse.of(histories);
    }
}
