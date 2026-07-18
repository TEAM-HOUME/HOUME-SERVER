package or.sopt.houme.taste.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 취향(무드보드) 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 *
 * <p>기존 {@code tastes} 테이블(id, url, filename, original_filename, file_extension)의 도메인 표현이다.
 */
@Getter
public class Taste {

    private final Long id;
    private final String url;
    private final String filename;
    private final String originalFilename;
    private final String fileExtension;

    @Builder
    private Taste(Long id, String url, String filename, String originalFilename, String fileExtension) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileExtension = fileExtension;
    }

    /** 신규 생성 (아직 영속화 전이므로 id 없음). */
    public static Taste of(String url, String filename, String originalFilename, String fileExtension) {
        return new Taste(null, url, filename, originalFilename, fileExtension);
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static Taste reconstitute(Long id, String url, String filename, String originalFilename, String fileExtension) {
        return new Taste(id, url, filename, originalFilename, fileExtension);
    }
}
