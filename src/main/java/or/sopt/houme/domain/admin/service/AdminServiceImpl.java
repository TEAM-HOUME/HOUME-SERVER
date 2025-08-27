package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminLoginRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.admin.entity.Admin;
import or.sopt.houme.domain.admin.repository.AdminRepository;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final BCrypt bCrypt;
    private final JWTUtil jwtUtil;


    @Override
    public void signUp(AdminSignUpRequestDTO dto) {

        Admin newAdmin = Admin.builder()
                .username(dto.username())
                .password(bCrypt.hash(dto.password()))
                .role(Role.ROLE_ADMIN)
                .build();

        adminRepository.save(newAdmin);
    }

    @Override
    public String login(AdminLoginRequestDTO adminLoginRequestDTO) {
        Admin admin = adminRepository.findByUsername(adminLoginRequestDTO.email())
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        if (!bCrypt.isMatch(adminLoginRequestDTO.password(), admin.getPassword())) {
            throw new GeneralException(ErrorCode.USER_NOT_FOUND);
        }

        // 24시간
        long expiredMs = 24 * 60 * 60;

        return jwtUtil.createJwt("access", admin.getId(), admin.getRole().name(), expiredMs);
    }
}
