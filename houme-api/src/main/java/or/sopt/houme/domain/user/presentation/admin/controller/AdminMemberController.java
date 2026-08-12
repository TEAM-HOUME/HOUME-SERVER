package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.request.AdminCreditGrantRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminCreditGrantResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminMemberSearchResponse;
import or.sopt.houme.domain.user.service.admin.AdminMemberService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/members")
@Tag(name = "어드민 회원 API")
@Validated
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    @Operation(summary = "회원 검색 API (이메일 완전일치 또는 닉네임 부분검색, 현재 크레딧 잔액 포함)")
    public ResponseEntity<ApiResponse<AdminMemberSearchResponse>> searchMembers(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminMemberService.searchMembers(keyword)));
    }

    @PostMapping("/{memberId}/credits")
    @Operation(summary = "회원 크레딧 지급 API")
    public ResponseEntity<ApiResponse<AdminCreditGrantResponse>> grantCredits(
            @PathVariable Long memberId,
            @Valid @RequestBody AdminCreditGrantRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminMemberService.grantCredits(memberId, request.amount())));
    }
}
