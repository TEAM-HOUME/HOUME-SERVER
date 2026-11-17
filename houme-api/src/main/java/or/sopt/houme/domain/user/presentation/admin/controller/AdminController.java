package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminLoginRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminLoginResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.user.service.admin.AdminFurnitureService;
import or.sopt.houme.domain.user.service.admin.AdminService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 회원 관련 API")
public class AdminController {

    private final AdminService adminService;


/*    @PostMapping("/register")
    @Operation(summary = "어드민 회원가입 API")
    public void register(@RequestBody AdminSignUpRequestDTO dto) {
        adminService.signUp(dto);
    }*/


    @PostMapping("/login")
    @Operation(summary = "어드민 로그인 API",
            description = "백오피스 클라이언트용 로그인입니다. 응답의 accessToken을 Authorization: Bearer 헤더로 사용합니다.\n" +
                    "- 토큰은 24시간 유효하며 리프레시 토큰이 없습니다. 만료(40002)되면 다시 로그인해야 합니다.\n" +
                    "- 아이디 또는 비밀번호가 틀리면 40401을 반환합니다.")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(@RequestBody @Valid AdminLoginRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.login(request)));
    }


    @GetMapping("/test")
    @Operation(summary = "어드민 권한 테스트 API")
    public ResponseEntity<ApiResponse<String>> adminOnlyTest() {
        return ResponseEntity.ok(ApiResponse.ok("ADMIN API가 성공적으로 동작합니다"));
    }
}
