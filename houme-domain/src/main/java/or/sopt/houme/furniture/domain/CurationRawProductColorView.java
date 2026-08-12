package or.sopt.houme.furniture.domain;

/**
 * 큐레이션 원본상품 색상 read model. 색상명 결정 규칙(클라이언트명 우선, 없으면 원본명)을 도메인이 소유한다.
 */
public record CurationRawProductColorView(Long rawProductId, String clientColorName, String rawColorName) {

    /** 노출 색상명: clientColorName 우선, 비어있으면 rawColorName, 둘 다 없으면 null. */
    public String resolveColorName() {
        if (clientColorName != null && !clientColorName.isBlank()) {
            return clientColorName;
        }
        if (rawColorName != null && !rawColorName.isBlank()) {
            return rawColorName;
        }
        return null;
    }
}
