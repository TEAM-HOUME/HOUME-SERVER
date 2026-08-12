package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminLoginRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.user.model.admin.entity.Admin;
import or.sopt.houme.domain.user.repository.admin.AdminRepository;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final BCrypt bCrypt;
    private final JWTUtil jwtUtil;


    @Override
    public void signUp(AdminSignUpRequestDTO dto) {

        Optional<Admin> byUsername = adminRepository.findByUsername(dto.username());

        if (byUsername.isPresent()) {
            throw new UserException(ErrorCode.USERNAME_DUPLICATE);
        }

        Admin newAdmin = Admin.builder()
                .username(dto.username())
                .password(bCrypt.hash(dto.password()))
                .role(Role.ROLE_ADMIN)
                .build();

        adminRepository.save(newAdmin);
    }

    /**
     *
     * 로그인이 완료되면 24시간동안 유지되는 액세스 토큰을 발급합니다.
     *
     * ADMIN의 경우 사용빈도와 탈취 위험을 고려했을 때, 리프레시 토큰까지의 보안정책이 불필요하다고 생각하여
     * 단일토큰 정책으로 구현하였습니다.
     * */
    @Override
    public String login(AdminLoginRequestDTO adminLoginRequestDTO) {
        Admin admin = adminRepository.findByUsername(adminLoginRequestDTO.email())
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        if (!bCrypt.isMatch(adminLoginRequestDTO.password(), admin.getPassword())) {
            throw new GeneralException(ErrorCode.USER_NOT_FOUND);
        }

        long expiredMs = 24 * 60 * 60;

        return jwtUtil.createJwt("access", admin.getId(), admin.getRole().name(), expiredMs);
    }
}
