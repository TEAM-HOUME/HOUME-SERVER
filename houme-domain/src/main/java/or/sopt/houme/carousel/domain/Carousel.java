package or.sopt.houme.carousel.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 캐러셀(추천 이미지) 순수 도메인 모델. JPA 어노테이션이 없으며, 타입은 id 참조로만 표현한다.
 *
 * <p>기존 {@code carousels} 테이블(id, url, filename, original_filename, file_extension, carousel_type_id)의
 * 도메인 표현이다. CarouselType 은 @ManyToOne 대신 carouselTypeId 로만 참조한다.
 */
@Getter
public class Carousel {

    private final Long id;
    private final String url;
    private final String filename;
    private final String originalFilename;
    private final String fileExtension;
    private final Long carouselTypeId;

    @Builder
    private Carousel(Long id, String url, String filename, String originalFilename, String fileExtension, Long carouselTypeId) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileExtension = fileExtension;
        this.carouselTypeId = carouselTypeId;
    }

    public static Carousel reconstitute(Long id, String url, String filename, String originalFilename,
                                        String fileExtension, Long carouselTypeId) {
        return new Carousel(id, url, filename, originalFilename, fileExtension, carouselTypeId);
    }
}
