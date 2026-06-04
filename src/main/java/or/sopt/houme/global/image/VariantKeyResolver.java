package or.sopt.houme.global.image;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 원본 이미지 key와 최적화 variant key 사이의 네이밍 규칙을 정의합니다.
 * 이 규칙은 서버와 웹이 동일하게 공유해야 합니다.
 *
 * 규칙: {원본 key에서 확장자 제거}__w{너비}.webp
 * 예) floorplan/1712_ab12cd34.png → floorplan/1712_ab12cd34__w400.webp
 */
@Component
public class VariantKeyResolver {

    /**
     * 생성할 variant 너비
     * 프론트 렌더에 필요한 크기 기반(그리드 썸네일 ~200px, 최대너비/상세이미지 ~440px)에 DPR 2~3을 반영한 값.
     * 웹의 srcset이 페이지별로 필요한 이미지 1개만 다운로드하므로, 모든 이미지에 3가지 width를 적용해도 대역폭 낭비가 없습니다.
     */
    public static final List<Integer> VARIANT_WIDTHS = List.of(400, 800, 1280);

    private static final String VARIANT_MARKER = "__w";
    private static final String WEBP_EXTENSION = ".webp";
    // variant key 판별용: "..__w{숫자}.webp" 로 끝나는 key
    private static final Pattern VARIANT_KEY_PATTERN = Pattern.compile(".*__w\\d+\\.webp$");

    /**
     * 원본 key와 너비로 variant key를 만듭니다.
     *
     * @param originalKey 원본 S3 key (예: floorplan/1712_ab.png)
     * @param width       variant 가로 너비
     * @return variant key (예: floorplan/1712_ab__w400.webp)
     */
    public String toVariantKey(String originalKey, int width) {
        return stripExtension(originalKey) + VARIANT_MARKER + width + WEBP_EXTENSION;
    }

    /**
     * 주어진 key가 variant(최적화로 생성된 webp)인지 판별합니다.
     * 호출 측에서 variant를 걸러낼 때 사용합니다. (variant를 다시 변환하는 일 방지)
     */
    public boolean isVariantKey(String key) {
        return key != null && VARIANT_KEY_PATTERN.matcher(key).matches();
    }

    /**
     * key에서 파일 확장자를 제거합니다. 디렉토리 경로에 '.'이 있는 경우를 방어합니다.
     */
    private String stripExtension(String key) {
        int lastDot = key.lastIndexOf('.');
        int lastSlash = key.lastIndexOf('/');
        if (lastDot > lastSlash) {
            return key.substring(0, lastDot);
        }
        return key;
    }
}
