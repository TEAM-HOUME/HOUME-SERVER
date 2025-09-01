package or.sopt.houme.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.admin.service.AdminFurnitureService;
import or.sopt.houme.domain.admin.service.AdminService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 관련 API")
public class AdminController {

    private final AdminService adminService;
    private final AdminFurnitureService adminFurnitureService;


    @PostMapping("/register")
    @Operation(description = "어드민 회원가입 API")
    public void register(@RequestBody AdminSignUpRequestDTO dto) {
        adminService.signUp(dto);
    }


    @GetMapping("/test")
    @Operation(description = "어드민 권한 테스트 API")
    public ResponseEntity<ApiResponse<String>> adminOnlyTest() {
        return ResponseEntity.ok(ApiResponse.ok("ADMIN API가 성공적으로 동작합니다"));
    }


    @PostMapping("/furniture")
    @Operation(description = "가구 등록 API")
    public ResponseEntity<ApiResponse<String>> adminFurniture(@RequestBody AdminFurnitureRequestDTO dto) {

        adminFurnitureService.registerFurniture(dto);
        return ResponseEntity.ok(ApiResponse.ok("가구가 등록되었습니다"));

    }


    @PostMapping("/furniture/prompt")
    @Operation(description = "가구 프롬프트 등록 API")
    public ResponseEntity<ApiResponse<String>> adminFurniturePrompt(@RequestBody AdminFurniturePromptRequestDTO dto) {
        adminFurnitureService.registerFurniturePrompt(dto);

        return ResponseEntity.ok(ApiResponse.ok("가구가 등록되었습니다"));
    }


    @GetMapping("/furnitures")
    @Operation(description = "가구 조회 API")
    public ResponseEntity<ApiResponse<AdminFurnitureGetDTO>> getFurnitures() {
        AdminFurnitureGetDTO furniture = adminFurnitureService.getFurniture();
        return ResponseEntity.ok(ApiResponse.ok(furniture));
    }


    @GetMapping("/furniture/tags")
    @Operation(description = "스타일 태그 조회 API")
    public ResponseEntity<ApiResponse<AdminFurnitureTagGetDTO>> getFurnitureTags() {

        AdminFurnitureTagGetDTO result = adminFurnitureService.getFurnitureTag();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }


    @PatchMapping("/furniture")
    @Operation(description = "가구 정보 수정 API")
    public ResponseEntity<ApiResponse<String>> updateFurniture(@RequestBody AdminFurnitureUpdateRequestDTO dto) {
        adminFurnitureService.updateFurniture(dto);

        return ResponseEntity.ok(ApiResponse.ok("업데이트가 성공적으로 완료되었습니다"));
    }


    @DeleteMapping("/furniture")
    @Operation(description = "가구 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteFurniture(@RequestBody AdminFurnitureDeleteDTO dto) {

        adminFurnitureService.deleteFurniture(dto);
        return ResponseEntity.ok(ApiResponse.ok("삭제가 성공적으로 완료되었습니다"));
    }

}
