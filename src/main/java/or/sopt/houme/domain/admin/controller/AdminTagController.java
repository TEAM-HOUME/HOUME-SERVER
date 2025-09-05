package or.sopt.houme.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagRequestDTO;
import or.sopt.houme.domain.admin.service.AdminTagService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 스타일 태그 관련 API")
public class AdminTagController {

    private final AdminTagService adminTagService;

    @PostMapping("/tag")
    @Operation(summary = "스타일 태그 생성 API")
    public ResponseEntity<ApiResponse<String>> createTag(AdminTagRequestDTO dto){
        adminTagService.create(dto);

        return ResponseEntity.ok(ApiResponse.ok("성공적으로 스타일 태그가 생성 되었습니다"));
    }
}
