package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.user.service.admin.AdminFurnitureService;
import or.sopt.houme.domain.user.service.admin.AdminService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 회원 관련 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;


/*    @PostMapping("/register")
    @Operation(summary = "어드민 회원가입 API")
    public void register(@RequestBody AdminSignUpRequestDTO dto) {
        adminService.signUp(dto);
    }*/


    @GetMapping("/test")
    @Operation(summary = "어드민 권한 테스트 API")
    public ResponseEntity<ApiResponse<String>> adminOnlyTest() {
        return ResponseEntity.ok(ApiResponse.ok("ADMIN API가 성공적으로 동작합니다"));
    }
}
