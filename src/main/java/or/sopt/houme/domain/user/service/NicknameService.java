package or.sopt.houme.domain.user.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class NicknameService {

    private static final List<String> IDIOMS = List.of(
            "잠자는",
            "반짝이는",
            "느긋한",
            "성실한",
            "유쾌한",
            "차분한",
            "당당한",
            "기민한",
            "용감한",
            "행복한"
    );

    private static final List<String> NOUNS = List.of(
            "고양이",
            "호랑이",
            "고래",
            "독수리",
            "사자",
            "여우",
            "토끼",
            "다람쥐",
            "펭귄",
            "돌고래"
    );

    public String rotateNickname() {
        return randomWord(IDIOMS) + randomWord(NOUNS) + randomNumberSuffix();
    }

    private String randomWord(List<String> words) {
        int index = ThreadLocalRandom.current().nextInt(words.size());
        return words.get(index);
    }

    private String randomNumberSuffix() {
        int number = ThreadLocalRandom.current().nextInt(10_000);
        return String.format("%04d", number);
    }
}
