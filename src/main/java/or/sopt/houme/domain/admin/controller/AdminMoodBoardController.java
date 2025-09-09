package or.sopt.houme.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardGetAllResponseDTO;
import or.sopt.houme.domain.admin.service.AdminMoodBoardService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "어드민 무드보드 관련 API")
public class AdminMoodBoardController {


    private final AdminMoodBoardService adminMoodBoardService;


    @PostMapping("/moodboard")
    @Operation(summary = "무드보드 이미지 업로드용 presigned url 생성 API")
    public ResponseEntity<ApiResponse<AdminMoodBoardCreateResponseDTO>> createMoodBoard(
            @RequestBody AdminMoodBoardCreateRequestDTO requestDTO,
            @RequestParam("contentType") String contentType) {

        AdminMoodBoardCreateResponseDTO response = adminMoodBoardService.create(requestDTO, contentType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }


    @GetMapping("/moodboards")
    @Operation(summary = "무드보드 조회 API")
    public ResponseEntity<ApiResponse<AdminMoodBoardGetAllResponseDTO>> getAll(){
        AdminMoodBoardGetAllResponseDTO response = adminMoodBoardService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }


    @DeleteMapping("/moodboard")
    @Operation(summary = "무드보드 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteMoodBoard(@RequestParam String filename){

        adminMoodBoardService.delete(filename);
        return ResponseEntity.ok(ApiResponse.ok("무드보드 삭제에 성공하였습니다"));
    }
}
