package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import java.util.List;

public record AdminFurnitureOptionListResponse(
        List<AdminFurnitureOptionResponse> furnitures
) {
    public static AdminFurnitureOptionListResponse of(List<AdminFurnitureOptionResponse> furnitures) {
        return new AdminFurnitureOptionListResponse(furnitures);
    }
}
