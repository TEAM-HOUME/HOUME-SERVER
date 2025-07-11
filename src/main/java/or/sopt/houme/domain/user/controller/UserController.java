package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "회원 관련 api")
public class UserController {
    private final UserService userService;

    @GetMapping(value = "/mypage/user")  // 유저의 이름, 사용가능한 크레딧 개수 조회
    @Operation(summary = "마이페이지 기본 정보 제공 API")
    public ResponseEntity<ApiResponse<MyPageInfoResponse>> getMyPageInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyPageInfoResponse myPageInfoResponse = userService.getMyPageInfo(userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.ok(myPageInfoResponse));
    }

    @GetMapping(value = "/mypage/images")
    @Operation(summary = "마이페이지 이미지 생성 이력 제공 API")
    public ResponseEntity<ApiResponse<UserImageHistoryListResponse>> getUserImageHistoryList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserImageHistoryListResponse userImageHistoryListResponse = userService.getUserImageHistoryList(userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.ok(userImageHistoryListResponse));
    }

    @GetMapping(value = "/mypage/images/{imageId}")
    @Operation(summary = "마이페이지에서 이미지 생성 이력 클릭시 결과 페이지 제공 API")
    public ResponseEntity<ApiResponse<ImageHistoryResultPageResponse>> getImageHistoryResultPage(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long imageId) {
        ImageHistoryResultPageResponse imageHistoryResultPageResponse = userService.getImageHistoryResultPage(userDetails.getUser(), imageId);

        return ResponseEntity.ok(ApiResponse.ok(imageHistoryResultPageResponse));
    }

    @PatchMapping(value = "/sign-up")
    @Operation(summary = "자체 회원가입 API")
    public ResponseEntity<ApiResponse<Void>> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid CreateUserRequest createUserRequest) {
        userService.updateUser(userDetails.getUser(), createUserRequest);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
