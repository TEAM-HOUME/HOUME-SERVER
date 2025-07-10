package or.sopt.houme.domain.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.address.dto.request.AddressRequest;
import or.sopt.houme.domain.address.service.AddressService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "주소 등록 API")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "사용자 주소 입력받기 API",
            description = "사용자가 유사한 도면 템플릿이 없는 경우, 주소를 등록 할 수 있습니다.")
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Void>> createAddress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @RequestBody @Valid AddressRequest addressRequest) {

        User user = userDetails.getUser();

        addressService.createAddress(user, addressRequest);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
