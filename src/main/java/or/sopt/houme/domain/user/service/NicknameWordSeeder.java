package or.sopt.houme.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.model.entity.NicknameWord;
import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import or.sopt.houme.domain.user.repository.NicknameWordRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NicknameWordSeeder implements ApplicationRunner {

    private final NicknameWordRepository nicknameWordRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:nickname/nickname-seed.json")
    private Resource nicknameSeedResource;

    @Override
    public void run(ApplicationArguments args) {
        if (nicknameWordRepository.count() > 0) {
            return;
        }

        NicknameSeed nicknameSeed = loadSeed();
        List<NicknameWord> nicknameWords = new ArrayList<>();

        addWords(nicknameWords, NicknameWordType.ADJECTIVE, nicknameSeed.adjectives());
        addWords(nicknameWords, NicknameWordType.NOUN, nicknameSeed.nouns());

        if (nicknameWords.isEmpty()) {
            throw new UserException(ErrorCode.NICKNAME_RESOURCE_EMPTY);
        }

        nicknameWordRepository.saveAll(nicknameWords);
        log.info("닉네임 시드 데이터를 적재합니다. count={}", nicknameWords.size());
    }

    private NicknameSeed loadSeed() {
        try (InputStream inputStream = nicknameSeedResource.getInputStream()) {
            return objectMapper.readValue(inputStream, NicknameSeed.class);
        } catch (IOException exception) {
            throw new UserException(ErrorCode.NICKNAME_RESOURCE_LOAD_FAIL);
        }
    }

    private void addWords(List<NicknameWord> target, NicknameWordType type, List<String> words) {
        if (words == null) {
            return;
        }

        Set<String> deduplicatedWords = new LinkedHashSet<>(words);
        deduplicatedWords.stream()
                .map(String::trim)
                .filter(word -> !word.isBlank())
                .map(word -> NicknameWord.of(type, word))
                .forEach(target::add);
    }

    private record NicknameSeed(
            List<String> adjectives,
            List<String> nouns
    ) {
    }
}
