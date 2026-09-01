package or.sopt.houme.compare.domain.port.out;

/** Gemini에 임시 점검용 텍스트 프롬프트를 전달하는 아웃바운드 포트입니다. */
public interface GeminiPromptPort {

    String generate(String prompt);
}
