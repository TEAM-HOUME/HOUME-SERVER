package or.sopt.houme.domain.furniture.service.admin;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductExposureUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductColorOptionResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductFurnitureTagResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;

import java.util.List;

public interface AdminCurationRawProductService {

    AdminCurationRawProductListResponse getAll(
            int page,
            int size,
            SoozipCategory category,
            Long minListPrice,
            Long maxListPrice
    );

    AdminCurationRawProductResponse getById(Long curationRawProductId);

    List<AdminCurationRawProductColorOptionResponse> getColorOptions();

    AdminCurationRawProductResponse create(AdminCurationRawProductCreateRequest request);

    AdminCurationRawProductResponse update(Long curationRawProductId, AdminCurationRawProductUpdateRequest request);

    void updateExposure(AdminCurationRawProductExposureUpdateRequest request);

    AdminCurationRawProductFurnitureTagResponse createFurnitureTagMapping(
            Long curationRawProductId,
            AdminCurationRawProductFurnitureTagCreateRequest request
    );

    AdminCurationRawProductFurnitureTagResponse updateFurnitureTagMapping(
            Long curationRawProductId,
            Long mappingId,
            AdminCurationRawProductFurnitureTagUpdateRequest request
    );

    void deleteFurnitureTagMapping(Long curationRawProductId, Long mappingId);

    void delete(Long curationRawProductId);
}
