package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.NicknameWord;
import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import or.sopt.houme.domain.user.repository.NicknameWordRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class NicknameService {
    private static final int NICKNAME_TAG_RETRY_COUNT = 20;

    private final NicknameWordRepository nicknameWordRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String rotateNickname() {
        List<NicknameWord> adjectives = nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.ADJECTIVE);
        List<NicknameWord> nouns = nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.NOUN);

        return randomWord(adjectives) + randomWord(nouns);
    }

    @Transactional(readOnly = true)
    public String generateNicknameTag(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new UserException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        for (int attempt = 0; attempt < NICKNAME_TAG_RETRY_COUNT; attempt++) {
            String nicknameTag = randomNumberTag();
            if (!Boolean.TRUE.equals(userRepository.existsByNicknameAndNicknameTag(nickname, nicknameTag))) {
                return nicknameTag;
            }
        }

        throw new UserException(ErrorCode.NICKNAME_TAG_GENERATION_FAILED);
    }

    private String randomWord(List<NicknameWord> words) {
        if (words.isEmpty()) {
            throw new UserException(ErrorCode.NICKNAME_RESOURCE_EMPTY);
        }

        int index = ThreadLocalRandom.current().nextInt(words.size());
        return words.get(index).getWord();
    }

    private String randomNumberTag() {
        int number = ThreadLocalRandom.current().nextInt(10_000);
        return "#" + String.format("%04d", number);
    }
}
