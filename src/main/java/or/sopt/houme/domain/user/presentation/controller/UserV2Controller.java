package or.sopt.houme.domain.user.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.presentation.controller.dto.CreateUserV2Request;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.presentation.controller.dto.SocialSignUpV2Request;
import or.sopt.houme.domain.user.presentation.controller.dto.UpdateMyPageProfileRequest;
import or.sopt.houme.domain.user.presentation.controller.dto.UpdateMyPageProfileResponse;
import or.sopt.houme.domain.user.service.NicknameService;
import or.sopt.houme.domain.user.service.OAuthService;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Tag(name = "회원 관련 api v2")
public class UserV2Controller {

    private final UserService userService;
    private final OAuthService oAuthService;
    private final NicknameService nicknameService;

    @PatchMapping(value = "/sign-up")
    @Operation(summary = "자체 회원가입 API v2")
    public ResponseEntity<ApiResponse<String>> updateUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateUserV2Request createUserV2Request
    ) {
        Gender gender = parseGender(createUserV2Request.gender());
        LocalDate birthday = parseBirthday(createUserV2Request.birthday());

        String nickname = userService.updateUserV2(userDetails.getUser(), createUserV2Request.nickname(), gender, birthday);

        return ResponseEntity.ok(ApiResponse.ok(nickname));
    }

    @PostMapping(value = "/sign-up")
    @Operation(summary = "소셜 자체 회원가입 API v2",
            description = "카카오 소셜로그인 완료 후 발급된 signupToken(임시토큰)과 함께 호출하면 회원을 생성합니다. <br><br>" +
                    "성공 시 access-token 헤더와 refresh-token 쿠키를 함께 반환합니다.")
    public ResponseEntity<ApiResponse<String>> signUp(
            @RequestBody @Valid SocialSignUpV2Request signUpRequest,
            HttpServletResponse response
    ) {
        Gender gender = parseGender(signUpRequest.gender());
        LocalDate birthday = parseBirthday(signUpRequest.birthday());

        String nickname = oAuthService.signUpWithTokenV2(
                signUpRequest.signupToken(),
                signUpRequest.nickname(),
                gender,
                birthday,
                response
        );

        return ResponseEntity.ok(ApiResponse.ok(nickname));
    }

    @GetMapping("/nickname/rotate")
    @Operation(summary = "닉네임 랜덤 생성 API")
    public ResponseEntity<ApiResponse<String>> rotateNickname() {
        return ResponseEntity.ok(ApiResponse.ok(nicknameService.rotateNickname()));
    }

    @PatchMapping(value = "/mypage/user")
    @Operation(summary = "마이페이지 프로필 수정 API")
    public ResponseEntity<ApiResponse<UpdateMyPageProfileResponse>> updateMyPageProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateMyPageProfileRequest request
    ) {
        Gender gender = parseGender(request.gender());
        LocalDate birthday = parseBirthday(request.birthday());

        UpdateMyPageProfileResponse response = userService.updateMyPageProfile(
                userDetails.getUser(),
                request.nickname(),
                gender,
                birthday
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping(value = "/mypage/images")
    @Operation(summary = "마이페이지 생성 이미지 이력 v2 제공 API")
    public ResponseEntity<ApiResponse<MyPageGeneratedImageV2Response>> getUserImageHistoryListV2(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyPageGeneratedImageV2Response response = userService.getUserGeneratedImageHistoryListV2(userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender);
        } catch (Exception e) {
            throw new UserException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }

    private LocalDate parseBirthday(String birthday) {
        try {
            return LocalDate.parse(birthday);
        } catch (Exception e) {
            throw new UserException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }
}
