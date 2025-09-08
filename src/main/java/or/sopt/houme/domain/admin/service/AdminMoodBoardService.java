package or.sopt.houme.domain.admin.service;

import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;

public interface AdminMoodBoardService {
    AdminMoodBoardCreateResponseDTO create(AdminMoodBoardCreateRequestDTO requestDTO, String contentType);
}