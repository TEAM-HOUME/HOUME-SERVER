package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;

import java.util.List;

public interface KeywordTranslationPort {

    String translateToEnglish(String koreanProductName);

    MarketplaceSearchKeywords translateToMarketplaceKeywords(
            String koreanProductName,
            List<FurnitureWithTypeView> furnitureCandidates
    );
}
