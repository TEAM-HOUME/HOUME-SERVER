package or.sopt.houme.furniture.domain;

import lombok.Builder;
import lombok.Getter;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;

/**
 * 추천가구 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 * enum(CurationSource)은 순수 타입이라 기존 패키지를 공유한다(P2 물리 분리 시 domain 모듈로 이동).
 */
@Getter
@Builder
public class RecommendFurniture {

    private final Long id;
    private final String furnitureProductImageUrl;
    private final String furnitureProductSiteUrl;
    private final String furnitureProductName;
    private final String furnitureProductMallName;
    private final Long furnitureProductId;
    private final CurationSource source;

    /** 신규 생성 (아직 영속화 전이므로 id 없음). */
    public static RecommendFurniture from(
            String furnitureProductImageUrl,
            String furnitureProductSiteUrl,
            String furnitureProductName,
            String furnitureProductMallName,
            Long furnitureProductId,
            CurationSource source
    ) {
        return RecommendFurniture.builder()
                .furnitureProductImageUrl(furnitureProductImageUrl)
                .furnitureProductSiteUrl(furnitureProductSiteUrl)
                .furnitureProductName(furnitureProductName)
                .furnitureProductMallName(furnitureProductMallName)
                .furnitureProductId(furnitureProductId)
                .source(source)
                .build();
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static RecommendFurniture reconstitute(Long id, String imageUrl, String siteUrl, String name,
                                                  String mallName, Long furnitureProductId, CurationSource source) {
        return RecommendFurniture.builder()
                .id(id)
                .furnitureProductImageUrl(imageUrl)
                .furnitureProductSiteUrl(siteUrl)
                .furnitureProductName(name)
                .furnitureProductMallName(mallName)
                .furnitureProductId(furnitureProductId)
                .source(source)
                .build();
    }
}
