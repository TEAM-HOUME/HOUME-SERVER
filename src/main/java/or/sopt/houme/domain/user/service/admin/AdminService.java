package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminLoginRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminSignUpRequestDTO;

public interface AdminService {

    void signUp(AdminSignUpRequestDTO dto);

    String login(AdminLoginRequestDTO adminLoginRequestDTO);
}
