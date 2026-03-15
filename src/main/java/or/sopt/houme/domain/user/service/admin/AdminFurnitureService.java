package or.sopt.houme.domain.user.service.admin;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.type.response.AdminFurnitureTypeListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.type.request.AdminFurnitureTypeRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.type.request.AdminUpdateFurnitureTypeRequest;

public interface AdminFurnitureService {

    void registerFurniture(AdminFurnitureRequestDTO dto);

    AdminFurniturePromptCreateResponseDTO registerFurniturePrompt(AdminFurniturePromptRequestDTO dto, String contentType);

    AdminFurnitureGetDTO getFurniture();

    AdminFurnitureTagGetDTO getFurnitureTag();

    AdminFurnitureTagOptionListResponse getFurnitureTagsByType(Long furnitureTypeId);

    AdminFurnitureUpdateResponseDTO updateFurniture(AdminFurnitureUpdateRequestDTO dto, String contentType);

    void deleteFurnitureTag(AdminFurnitureTagDeleteDTO dto);

    void deleteFurniture(AdminFurnitureDeleteDTO dto);

    AdminFurnitureDetailsResponseDTO getDetails(AdminFurnitureDetailsRequestDTO dto);

    // 가구 타입 조회
    AdminFurnitureTypeListResponse getFurnitureTypes();

    // 가구 타입 추가
    void registerFurnitureType(AdminFurnitureTypeRequest request);

    // 가구 타입 삭제
    void deleteFurnitureType(long furnitureTypeId);

    // 가구 타입 수정
    void updateFurnitureType(AdminUpdateFurnitureTypeRequest request);
}
