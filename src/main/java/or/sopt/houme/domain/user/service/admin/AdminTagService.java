package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminTagUpdateRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagDeleteRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagGetAllResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagGetResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagRequestDTO;

import java.util.List;

public interface AdminTagService {
    void create(AdminTagRequestDTO dto);

    AdminTagGetAllResponseDTO getAll();

    void update(AdminTagUpdateRequestDTO dto);

    void delete(AdminTagDeleteRequestDTO dto);
}
