package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.client.NaverShopApiClient;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NaverShopService {

    private final NaverShopApiClient naverShopApiClient;

    public List<NaverFurnitureProductDto> search(String keyword, int size) {
        return naverShopApiClient.searchProducts(keyword, size);
    }
}
