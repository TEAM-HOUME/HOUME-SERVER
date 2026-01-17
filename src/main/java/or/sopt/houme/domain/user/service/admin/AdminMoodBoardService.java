package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardGetAllResponseDTO;

public interface AdminMoodBoardService {
    AdminMoodBoardCreateResponseDTO create(AdminMoodBoardCreateRequestDTO requestDTO, String contentType);

    AdminMoodBoardGetAllResponseDTO getAll();

    void delete(String filename);
}