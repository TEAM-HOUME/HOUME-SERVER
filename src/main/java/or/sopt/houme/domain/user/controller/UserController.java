package or.sopt.houme.domain.user.controller;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.controller.dto.ImageHistoryResultPageResponse;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.domain.user.service.UserServiceImpl;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    @GetMapping(value = "/mypage/user")  // 유저의 이름, 사용가능한 크레딧 개수 조회
    public ResponseEntity<ApiResponse<MyPageInfoResponse>> getMyPageInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyPageInfoResponse myPageInfoResponse = userService.getMyPageInfo(userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.ok(myPageInfoResponse));
    }

    @GetMapping(value = "/mypage/images")
    public ResponseEntity<ApiResponse<UserImageHistoryListResponse>> getUserImageHistoryList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserImageHistoryListResponse userImageHistoryListResponse = userService.getUserImageHistoryList(userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.ok(userImageHistoryListResponse));
    }

    @GetMapping(value = "/mypage/images/{imageId}")
    public ResponseEntity<ApiResponse<ImageHistoryResultPageResponse>> getImageHistoryResultPage(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long imageId) {
        ImageHistoryResultPageResponse imageHistoryResultPageResponse = userService.getImageHistoryResultPage(userDetails.getUser(), imageId);

        return ResponseEntity.ok(ApiResponse.ok(imageHistoryResultPageResponse));
    }
}
