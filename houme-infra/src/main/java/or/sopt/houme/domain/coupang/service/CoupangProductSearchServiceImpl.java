package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.coupang.service.CoupangProductSearchService;
import or.sopt.houme.domain.coupang.client.CoupangPartnersClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoupangProductSearchServiceImpl implements CoupangProductSearchService {

    private final CoupangPartnersClient coupangPartnersClient;

    @Override
    public List<CoupangProductSearchResult> search(String keyword, int limit) {
        return coupangPartnersClient.searchProducts(keyword, limit);
    }
}
