package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.service.JWTService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "테스트용 토큰 발급기 API")
public class JWTController {

    private final JWTService jwtService;

    @GetMapping("/access")
    @Operation(summary = "액세스 토큰 발급기",
            description = "자체 로그인이 없기 때문에 액세스 토큰이 필요한 경우에 해당 메서드를 이용하여 토큰을 발급 받아주세요")
    public void createAccess(HttpServletResponse response){
        jwtService.createToken(response);
    }


    @GetMapping("/access-test")
    @Operation(summary = "액세스 토큰 사용 방법",
            description = "**@AuthenticationPrincipal** 어노테이션을 통해서 회원정보를 가져옵니다 <br><br>" +
                    "이걸로 회원을 식별해주시면 됩니다")
    public String accessTest(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {

        String accessToken = request.getHeader("Authorization");

        log.info("Access Token: {}", accessToken);
        log.info("User's Name: {}",userDetails.getUser().getId());

        return "액세스 토큰이 성공적으로 작동합니다";
    }
}
