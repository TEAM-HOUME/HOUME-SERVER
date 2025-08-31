package or.sopt.houme.domain.admin.service;

import or.sopt.houme.domain.admin.controller.dto.AdminFurnitureGetDto;
import or.sopt.houme.domain.admin.controller.dto.AdminFurniturePromptRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.AdminFurnitureRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.AdminFurnitureTagGetDTO;
import or.sopt.houme.domain.furniture.entity.Furniture;

public interface AdminFurnitureService {

    void registerFurniture(AdminFurnitureRequestDTO dto);

    void registerFurniturePrompt (AdminFurniturePromptRequestDTO dto);

    AdminFurnitureGetDto getFurniture();

    AdminFurnitureTagGetDTO getFurnitureTag();
}
