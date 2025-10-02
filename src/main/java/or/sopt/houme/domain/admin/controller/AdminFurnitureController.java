package or.sopt.houme.domain.admin.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.response.AdminFurnitureTypeListResponse;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.request.AdminDeleteFurnitureTypeRequest;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.request.AdminFurnitureTypeRequest;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.request.AdminUpdateFurnitureTypeRequest;
import or.sopt.houme.domain.admin.service.AdminFurnitureService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 가구 관련 API")
public class AdminFurnitureController {

    private final AdminFurnitureService adminFurnitureService;

    @PostMapping("/furniture")
    @Operation(summary = "가구 등록 API")
    public ResponseEntity<ApiResponse<String>> adminFurniture(@RequestBody AdminFurnitureRequestDTO dto) {

        adminFurnitureService.registerFurniture(dto);
        return ResponseEntity.ok(ApiResponse.ok("가구가 등록되었습니다"));

    }

    @PostMapping("/furniture/type")
    @Operation(summary = "가구 타입 등록 API")
    public ResponseEntity<ApiResponse<String>> adminFurnitureType(@RequestBody AdminFurnitureTypeRequest dto) {

        adminFurnitureService.registerFurnitureType(dto);
        return ResponseEntity.ok(ApiResponse.ok("가구타입이 등록되었습니다."));
    }

    @PostMapping("/furniture/prompt")
    @Operation(summary = "가구 프롬프트 등록 API")
    public ResponseEntity<ApiResponse<String>> adminFurniturePrompt(@RequestBody AdminFurniturePromptRequestDTO dto) {
        adminFurnitureService.registerFurniturePrompt(dto);

        return ResponseEntity.ok(ApiResponse.ok("가구가 등록되었습니다"));
    }


    @GetMapping("/furnitures")
    @Operation(summary = "가구 조회 API")
    public ResponseEntity<ApiResponse<AdminFurnitureGetDTO>> getFurnitures() {
        AdminFurnitureGetDTO furniture = adminFurnitureService.getFurniture();
        return ResponseEntity.ok(ApiResponse.ok(furniture));
    }

    @GetMapping("/furniture/types")
    @Operation(summary = "가구 타입 조회 API")
    public ResponseEntity<ApiResponse<AdminFurnitureTypeListResponse>> getFurnitureTypes() {
        AdminFurnitureTypeListResponse furnitureTypes = adminFurnitureService.getFurnitureTypes();
        return ResponseEntity.ok(ApiResponse.ok(furnitureTypes));
    }

    @GetMapping("/furniture/tags")
    @Operation(summary = "스타일 태그 조회 API")
    public ResponseEntity<ApiResponse<AdminFurnitureTagGetDTO>> getFurnitureTags() {

        AdminFurnitureTagGetDTO result = adminFurnitureService.getFurnitureTag();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }


    @PatchMapping("/furniture")
    @Operation(summary = "가구 정보 수정 API")
    public ResponseEntity<ApiResponse<String>> updateFurniture(@RequestBody AdminFurnitureUpdateRequestDTO dto) {
        adminFurnitureService.updateFurniture(dto);

        return ResponseEntity.ok(ApiResponse.ok("업데이트가 성공적으로 완료되었습니다"));
    }

    @PatchMapping("/furniture/type")
    @Operation(summary = "가구 타입 정보 수정 API")
    public ResponseEntity<ApiResponse<String>> updateFurnitureType(@RequestBody AdminUpdateFurnitureTypeRequest dto) {
        adminFurnitureService.updateFurnitureType(dto);
        return ResponseEntity.ok(ApiResponse.ok("업데이트가 성공적으로 완료되었습니다"));
    }

    @DeleteMapping("/furniture/tag")
    @Operation(summary = "가구 태그 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteFurnitureTag(@RequestBody AdminFurnitureTagDeleteDTO dto) {

        adminFurnitureService.deleteFurnitureTag(dto);
        return ResponseEntity.ok(ApiResponse.ok("삭제가 성공적으로 완료되었습니다"));
    }


    @DeleteMapping("/furniture")
    @Operation(summary = "가구 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteFurniture(@RequestBody AdminFurnitureDeleteDTO dto) {
        adminFurnitureService.deleteFurniture(dto);
        return ResponseEntity.ok(ApiResponse.ok("삭제가 성공적으로 완료되었습니다"));
    }

    @DeleteMapping("/furniture/type")
    @Operation(summary = "가구 타입 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteFurnitureType(@RequestBody AdminDeleteFurnitureTypeRequest dto) {
        adminFurnitureService.deleteFurnitureType(dto.furnitureTypeId());
        return ResponseEntity.ok(ApiResponse.ok("삭제가 성공적으로 완료되었습니다"));
    }

    @GetMapping("/furniture/prompt")
    @Operation(summary = "가구 프롬프트 조회 API")
    public ResponseEntity<ApiResponse<AdminFurnitureDetailsResponseDTO>> getFurniturePrompt(@RequestParam String furnitureNameKr, @RequestParam Long tagId) {

        AdminFurnitureDetailsRequestDTO dto = new AdminFurnitureDetailsRequestDTO(furnitureNameKr, tagId);

        AdminFurnitureDetailsResponseDTO details = adminFurnitureService.getDetails(dto);
        return ResponseEntity.ok(ApiResponse.ok(details));
    }

}
