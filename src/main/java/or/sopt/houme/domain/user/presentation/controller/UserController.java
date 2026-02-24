package or.sopt.houme.domain.user.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.controller.dto.*;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.service.UserDeletionService;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.domain.user.service.OAuthService;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "회원 관련 api")
public class UserController {

    private final UserService userService;
    private final UserDeletionService userDeletionService;
    private final OAuthService oAuthService;

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

    @GetMapping(value = "/mypage/images/{houseId}")
    @Operation(summary = "마이페이지에서 이미지 생성 이력 클릭시 결과 페이지 제공 API")
    public ResponseEntity<ApiResponse<ImageHistoriesResultPageResponse>> getImageHistoryResultPage(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long houseId) {
        ImageHistoriesResultPageResponse imageHistoryResultPageResponse = userService.getImageHistoryResultPage(userDetails.getUser(), houseId);

        return ResponseEntity.ok(ApiResponse.ok(imageHistoryResultPageResponse));
    }

    @PatchMapping(value = "/sign-up")
    @Operation(summary = "자체 회원가입 API")
    public ResponseEntity<ApiResponse<String>> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid CreateUserRequest createUserRequest) {
        Gender gender;
        LocalDate birthday;

        try {
            gender = Gender.valueOf(createUserRequest.gender());
        } catch (IllegalArgumentException e){
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        try {
            birthday = LocalDate.parse(createUserRequest.birthday());
        } catch (IllegalArgumentException e){
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        String username = userService.updateUser(userDetails.getUser(), createUserRequest.name(), gender, birthday);

        return ResponseEntity.ok(ApiResponse.ok(username));
    }

    @PostMapping(value = "/sign-up")
    @Operation(summary = "소셜 자체 회원가입 API",
            description = "카카오 소셜로그인 완료 후 발급된 signupToken(임시토큰)과 함께 호출하면 회원을 생성합니다. <br><br>" +
                    "성공 시 access-token 헤더와 refresh-token 쿠키를 함께 반환합니다.")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody @Valid SocialSignUpRequest signUpRequest,
                                                      HttpServletResponse response) {
        Gender gender;
        LocalDate birthday;

        try {
            gender = Gender.valueOf(signUpRequest.gender());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        try {
            birthday = LocalDate.parse(signUpRequest.birthday());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        String username = oAuthService.signUpWithToken(
                signUpRequest.signupToken(),
                signUpRequest.name(),
                gender,
                birthday,
                response
        );

        return ResponseEntity.ok(ApiResponse.ok(username));
    }

    @DeleteMapping("/user")
    @Operation(summary = "회원 탈퇴 API",
    description = "회원을 삭제합니다. <br><br>" +
            "정책 상, 한 번 삭제된 회원은 **절대 되돌릴 수 없으니** 주의해주세요.")
    public ResponseEntity<ApiResponse<String>> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        userDeletionService.delete(userDetails.getUser().getId());

        return ResponseEntity.ok(ApiResponse.ok("회원이 정상적으로 삭제되었습니다."));
    }
}
