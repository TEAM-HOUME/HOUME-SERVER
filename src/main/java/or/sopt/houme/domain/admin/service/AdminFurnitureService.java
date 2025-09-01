package or.sopt.houme.domain.admin.service;

import or.sopt.houme.domain.admin.controller.dto.furniture.*;

public interface AdminFurnitureService {

    void registerFurniture(AdminFurnitureRequestDTO dto);

    void registerFurniturePrompt (AdminFurniturePromptRequestDTO dto);

    AdminFurnitureGetDto getFurniture();

    AdminFurnitureTagGetDTO getFurnitureTag();

    void updateFurniture(AdminFurnitureUpdateRequestDTO dto);
}
