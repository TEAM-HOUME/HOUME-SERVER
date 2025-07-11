package or.sopt.houme.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.controller.dto.*;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.domain.user.service.UserServiceImpl;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping(value = "/signup")
    public ResponseEntity<ApiResponse<Void>> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid CreateUserRequest createUserRequest) {
        userService.updateUser(userDetails.getUser(), createUserRequest);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
