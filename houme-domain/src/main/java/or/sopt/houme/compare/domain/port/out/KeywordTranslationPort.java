package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;

public interface KeywordTranslationPort {

    String translateToEnglish(String koreanProductName);

    MarketplaceSearchKeywords translateToMarketplaceKeywords(String koreanProductName);
}
