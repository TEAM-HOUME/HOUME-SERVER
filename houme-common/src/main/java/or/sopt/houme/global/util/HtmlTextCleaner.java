package or.sopt.houme.global.util;


// 아래 예시와 같은 태그를 제거하여 문자열을 정제합니다.
// 예시: THE PI 원형 <b>러그</b> 160CM <b>핑크색</b> 160CM<b>러그</b> 원형<b>러그</b> 거실<b>러그</b>
public class HtmlTextCleaner {
    private static final String REGEX_B_TAG = "(?i)</?b>";
    private static final String HTML_AMP = "&amp;";
    private static final String HTML_LT = "&lt;";
    private static final String HTML_GT = "&gt;";
    private static final String HTML_QUOTE = "&quot;";

    private static final String CHAR_AMP = "&";
    private static final String CHAR_LT = "<";
    private static final String CHAR_GT = ">";
    private static final String CHAR_QUOTE = "\"";

    private HtmlTextCleaner() {}

    public static String clean(String raw) {
        if (raw == null) return "";
        return raw.replaceAll(REGEX_B_TAG, "")
                .replace(HTML_AMP, CHAR_AMP)
                .replace(HTML_LT, CHAR_LT)
                .replace(HTML_GT, CHAR_GT)
                .replace(HTML_QUOTE, CHAR_QUOTE);
    }
}
