package or.sopt.houme.domain.credit.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.service.PaymentBtnClickLogService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "충전하기 버튼 클릭 로그 API")
@RequiredArgsConstructor
public class PaymentBtnClickLogController {
    private final PaymentBtnClickLogService paymentBtnClickLogService;

    @PostMapping(value = "/credits/logs")
    public ResponseEntity<ApiResponse<Void>> createPaymentBtnClickLog(@AuthenticationPrincipal CustomUserDetails userDetails) {
        paymentBtnClickLogService.createPaymentBtnClickLog(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
