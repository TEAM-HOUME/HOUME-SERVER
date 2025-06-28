package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.service.JWTService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "토큰 관련 API")
public class JWTController {

    private final JWTService jwtService;

    /**
     * 테스트용 액세스 토큰을 발급하여 HTTP 응답에 추가합니다.
     *
     * 자체 로그인 기능이 없으므로, 액세스 토큰이 필요한 경우 이 엔드포인트를 통해 토큰을 발급받을 수 있습니다.
     */
    @GetMapping("/access")
    @Operation(summary = "테스트용 액세스 토큰 발급기",
            description = "자체 로그인이 없기 때문에 액세스 토큰이 필요한 경우에 해당 메서드를 이용하여 토큰을 발급 받아주세요")
    public void createAccess(HttpServletResponse response){
        jwtService.createToken(response);
    }


    /**
     * 인증된 사용자의 정보를 확인하고 액세스 토큰의 유효성을 테스트하는 엔드포인트입니다.
     *
     * @param userDetails 인증된 사용자의 상세 정보
     * @param request HTTP 요청 객체
     * @return 액세스 토큰이 정상적으로 작동함을 알리는 성공 메시지의 ApiResponse
     */
    @GetMapping("/access-test")
    @Operation(summary = "액세스 토큰 사용 방법",
            description = "**@AuthenticationPrincipal** 어노테이션을 통해서 회원정보를 가져옵니다 <br><br>" +
                    "이걸로 회원을 식별해주시면 됩니다")
    public ResponseEntity<ApiResponse<String>> accessTest(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {

        String accessToken = request.getHeader("Authorization");

        log.info("Access Token: {}", accessToken);
        log.info("User's Name: {}",userDetails.getUser().getId());

        return ResponseEntity.ok(ApiResponse.ok("액세스 토큰이 성공적으로 작동합니다"));
    }

    /**
     * 리프레시 및 액세스 토큰을 재발급합니다.
     *
     * Refresh Rotate 로직을 사용하여 리프레시 토큰과 액세스 토큰을 모두 새로 발급합니다.
     * 만료된 액세스 토큰은 요청 헤더에서 제거한 후 호출해야 합니다.
     *
     * @return 토큰 재발급 성공 메시지를 포함한 ApiResponse
     */
    @PostMapping("/reissue")
    @Operation(
            summary = "리프레시,액세스 토큰 재발급 API",
            description = "리프레시,액세스 토큰 재발급 API입니다.<br><br>" +
                    "리프레시 토큰 탈취에 대비하여 액세스와 함께 리프레시 토큰도 재발급하는 Refresh Rotate 로직입니다 <br><br>" +
                    "**반드시 만료된 액세스토큰을 헤더에서 뺀 후**에 요청을 넣어주세요"
    )
    public ResponseEntity<ApiResponse<String>> reissue(HttpServletRequest request, HttpServletResponse response) {

        jwtService.RefreshRotate(request,response);

        return ResponseEntity.ok(ApiResponse.ok("성공적으로 재생성되었습니다"));
    }
}
