package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.NicknameWord;
import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import or.sopt.houme.domain.user.repository.NicknameWordRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class NicknameService {

    private final NicknameWordRepository nicknameWordRepository;

    @Transactional(readOnly = true)
    public String rotateNickname() {
        List<NicknameWord> adjectives = nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.ADJECTIVE);
        List<NicknameWord> nouns = nicknameWordRepository.findAllByTypeAndIsActiveTrue(NicknameWordType.NOUN);

        return randomWord(adjectives) + randomWord(nouns) + randomNumberSuffix();
    }

    private String randomWord(List<NicknameWord> words) {
        if (words.isEmpty()) {
            throw new UserException(ErrorCode.NICKNAME_RESOURCE_EMPTY);
        }

        int index = ThreadLocalRandom.current().nextInt(words.size());
        return words.get(index).getWord();
    }

    private String randomNumberSuffix() {
        int number = ThreadLocalRandom.current().nextInt(10_000);
        return String.format("%04d", number);
    }
}
