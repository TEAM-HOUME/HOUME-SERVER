package or.sopt.houme.domain.coupang.seed;

import or.sopt.houme.domain.coupang.service.CoupangSeedKeyword;

import java.util.List;

/** 하우미 가격 비교에서 주요 가구 카테고리를 고르게 훑기 위한 초기 100개 검색어입니다. */
public final class CoupangKeywordCatalog {

    private static final List<CoupangSeedKeyword> KEYWORDS = List.of(
            new CoupangSeedKeyword("3인용 소파", "SOFA"),
            new CoupangSeedKeyword("4인용 소파", "SOFA"),
            new CoupangSeedKeyword("패브릭 소파", "SOFA"),
            new CoupangSeedKeyword("가죽 소파", "SOFA"),
            new CoupangSeedKeyword("모듈 소파", "SOFA"),
            new CoupangSeedKeyword("리클라이너 소파", "SOFA"),
            new CoupangSeedKeyword("카우치 소파", "SOFA"),
            new CoupangSeedKeyword("소파베드", "SOFA"),
            new CoupangSeedKeyword("1인용 소파", "SOFA"),
            new CoupangSeedKeyword("코너형 소파", "SOFA"),

            new CoupangSeedKeyword("원목 식탁", "TABLE"),
            new CoupangSeedKeyword("4인용 식탁", "TABLE"),
            new CoupangSeedKeyword("6인용 식탁", "TABLE"),
            new CoupangSeedKeyword("세라믹 식탁", "TABLE"),
            new CoupangSeedKeyword("접이식 식탁", "TABLE"),
            new CoupangSeedKeyword("식탁 의자", "TABLE"),
            new CoupangSeedKeyword("아일랜드 식탁", "TABLE"),
            new CoupangSeedKeyword("사이드 테이블", "TABLE"),
            new CoupangSeedKeyword("거실 테이블", "TABLE"),
            new CoupangSeedKeyword("콘솔 테이블", "TABLE"),

            new CoupangSeedKeyword("퀸 침대", "BED"),
            new CoupangSeedKeyword("킹 침대", "BED"),
            new CoupangSeedKeyword("슈퍼싱글 침대", "BED"),
            new CoupangSeedKeyword("수납 침대", "BED"),
            new CoupangSeedKeyword("패밀리 침대", "BED"),
            new CoupangSeedKeyword("침대 프레임", "BED"),
            new CoupangSeedKeyword("저상형 침대", "BED"),
            new CoupangSeedKeyword("호텔식 침대", "BED"),
            new CoupangSeedKeyword("침대 협탁", "BED"),
            new CoupangSeedKeyword("침대 매트리스", "BED"),

            new CoupangSeedKeyword("5단 서랍장", "STORAGE"),
            new CoupangSeedKeyword("와이드 서랍장", "STORAGE"),
            new CoupangSeedKeyword("거실장", "STORAGE"),
            new CoupangSeedKeyword("TV 거실장", "STORAGE"),
            new CoupangSeedKeyword("수납장", "STORAGE"),
            new CoupangSeedKeyword("장식장", "STORAGE"),
            new CoupangSeedKeyword("주방 수납장", "STORAGE"),
            new CoupangSeedKeyword("틈새 수납장", "STORAGE"),
            new CoupangSeedKeyword("신발장", "STORAGE"),
            new CoupangSeedKeyword("화장대", "STORAGE"),

            new CoupangSeedKeyword("옷장", "WARDROBE"),
            new CoupangSeedKeyword("장롱", "WARDROBE"),
            new CoupangSeedKeyword("슬라이딩 옷장", "WARDROBE"),
            new CoupangSeedKeyword("시스템 행거", "WARDROBE"),
            new CoupangSeedKeyword("드레스룸 행거", "WARDROBE"),
            new CoupangSeedKeyword("오픈 행거", "WARDROBE"),
            new CoupangSeedKeyword("서랍형 옷장", "WARDROBE"),
            new CoupangSeedKeyword("이불장", "WARDROBE"),
            new CoupangSeedKeyword("붙박이장", "WARDROBE"),
            new CoupangSeedKeyword("옷걸이 행거", "WARDROBE"),

            new CoupangSeedKeyword("책상", "DESK"),
            new CoupangSeedKeyword("컴퓨터 책상", "DESK"),
            new CoupangSeedKeyword("L자 책상", "DESK"),
            new CoupangSeedKeyword("원목 책상", "DESK"),
            new CoupangSeedKeyword("높이조절 책상", "DESK"),
            new CoupangSeedKeyword("책장", "DESK"),
            new CoupangSeedKeyword("책상 책장 세트", "DESK"),
            new CoupangSeedKeyword("사무용 의자", "DESK"),
            new CoupangSeedKeyword("게이밍 의자", "DESK"),
            new CoupangSeedKeyword("스툴", "DESK"),

            new CoupangSeedKeyword("화장실 수납장", "BATHROOM"),
            new CoupangSeedKeyword("욕실 거울", "BATHROOM"),
            new CoupangSeedKeyword("욕실장", "BATHROOM"),
            new CoupangSeedKeyword("세면대 하부장", "BATHROOM"),
            new CoupangSeedKeyword("빨래바구니", "BATHROOM"),
            new CoupangSeedKeyword("욕실 선반", "BATHROOM"),
            new CoupangSeedKeyword("수건장", "BATHROOM"),
            new CoupangSeedKeyword("욕실 코너 선반", "BATHROOM"),
            new CoupangSeedKeyword("욕실 슬라이딩장", "BATHROOM"),
            new CoupangSeedKeyword("샤워부스 선반", "BATHROOM"),

            new CoupangSeedKeyword("협탁", "LIVING"),
            new CoupangSeedKeyword("수납 벤치", "LIVING"),
            new CoupangSeedKeyword("벤치 의자", "LIVING"),
            new CoupangSeedKeyword("1인 리클라이너", "LIVING"),
            new CoupangSeedKeyword("빈백 소파", "LIVING"),
            new CoupangSeedKeyword("라운지 체어", "LIVING"),
            new CoupangSeedKeyword("암체어", "LIVING"),
            new CoupangSeedKeyword("카페 의자", "LIVING"),
            new CoupangSeedKeyword("좌식 테이블", "LIVING"),
            new CoupangSeedKeyword("교자상", "LIVING"),

            new CoupangSeedKeyword("아기 침대", "KIDS"),
            new CoupangSeedKeyword("유아 옷장", "KIDS"),
            new CoupangSeedKeyword("유아 책상", "KIDS"),
            new CoupangSeedKeyword("유아 책장", "KIDS"),
            new CoupangSeedKeyword("키즈 소파", "KIDS"),
            new CoupangSeedKeyword("아동 수납장", "KIDS"),
            new CoupangSeedKeyword("아동 의자", "KIDS"),
            new CoupangSeedKeyword("벙커 침대", "KIDS"),
            new CoupangSeedKeyword("2층 침대", "KIDS"),
            new CoupangSeedKeyword("놀이방 수납장", "KIDS"),

            new CoupangSeedKeyword("야외 테이블", "OUTDOOR"),
            new CoupangSeedKeyword("캠핑 의자", "OUTDOOR"),
            new CoupangSeedKeyword("파라솔", "OUTDOOR"),
            new CoupangSeedKeyword("테라스 의자", "OUTDOOR"),
            new CoupangSeedKeyword("발코니 테이블", "OUTDOOR"),
            new CoupangSeedKeyword("야외 벤치", "OUTDOOR"),
            new CoupangSeedKeyword("정원 수납장", "OUTDOOR"),
            new CoupangSeedKeyword("라탄 의자", "OUTDOOR"),
            new CoupangSeedKeyword("흔들의자", "OUTDOOR"),
            new CoupangSeedKeyword("접이식 캠핑 테이블", "OUTDOOR")
    );

    private CoupangKeywordCatalog() {
    }

    public static List<CoupangSeedKeyword> defaults() {
        return KEYWORDS;
    }
}
