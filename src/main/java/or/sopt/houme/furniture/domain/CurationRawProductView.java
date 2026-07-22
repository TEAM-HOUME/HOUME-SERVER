package or.sopt.houme.furniture.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 큐레이션 원본상품 조회 전용 read model. 애플리케이션/타 도메인이 원본상품 엔티티 그래프 대신 소비한다.
 *
 * <p>쓰기(관리자 CRUD·크롤러)는 curation infra 가 엔티티로 직접 다루고, 경계를 넘는 읽기만 이 뷰로 나간다.
 */
@Getter
@Builder
public class CurationRawProductView {

    private final Long id;
    private final Long productId;
    private final String productName;
    private final String productImageUrl;
    private final String productSiteUrl;
    private final String productMallName;
    private final String brand;
    private final Long listPrice;
    private final Integer discountRate;
    private final Long discountPrice;
    private final LocalDateTime fetchedAt;
}
