package or.sopt.houme.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.service.UserLandingService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserLandingController {

    private final UserLandingService userLandingService;

    @GetMapping("/check-has-generated-image")
    public ResponseEntity<ApiResponse<Boolean>> checkHasGeneratedImage(HttpServletRequest request) {

        Boolean result = userLandingService.getHasGeneratedImage(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
