package or.sopt.houme.domain.furniture.service.admin;

import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;

public interface AdminCurationRawProductService {

    AdminCurationRawProductListResponse getAll(int page, int size);

    AdminCurationRawProductResponse getById(Long curationRawProductId);

    AdminCurationRawProductResponse create(AdminCurationRawProductCreateRequest request);

    AdminCurationRawProductResponse update(Long curationRawProductId, AdminCurationRawProductUpdateRequest request);

    void delete(Long curationRawProductId);
}
