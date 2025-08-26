package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminSignUpRequestDTO;
import or.sopt.houme.domain.admin.entity.Admin;
import or.sopt.houme.domain.admin.repository.AdminRepository;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.global.util.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final BCrypt bCrypt;


    @Override
    public void signUp(AdminSignUpRequestDTO dto) {

        Admin newAdmin = Admin.builder()
                .username(dto.username())
                .password(bCrypt.hash(dto.password()))
                .role(Role.ROLE_ADMIN)
                .build();

        adminRepository.save(newAdmin);
    }
}
