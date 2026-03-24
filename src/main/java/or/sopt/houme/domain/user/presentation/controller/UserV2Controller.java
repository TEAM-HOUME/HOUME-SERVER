package or.sopt.houme.domain.user.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Tag(name = "회원 관련 api v2")
public class UserV2Controller {

    private final UserService userService;

    @GetMapping(value = "/mypage/images")
    @Operation(summary = "마이페이지 생성 이미지 이력 v2 제공 API")
    public ResponseEntity<ApiResponse<MyPageGeneratedImageV2Response>> getUserImageHistoryListV2(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyPageGeneratedImageV2Response response = userService.getUserGeneratedImageHistoryListV2(userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
