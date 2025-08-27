package or.sopt.houme.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.admin.service.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 관련 API")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    @Operation(description = "어드민 회원가입 API")
    public void register(@RequestBody AdminSignUpRequestDTO dto) {
        adminService.signUp(dto);
    }

}
