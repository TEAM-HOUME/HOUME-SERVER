package or.sopt.houme.user.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.NicknameWord;
import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import or.sopt.houme.domain.user.repository.NicknameWordRepository;
import or.sopt.houme.user.domain.port.out.NicknameWordPort;
import org.springframework.stereotype.Component;

import java.util.List;

/** {@link NicknameWordPort} 의 JPA 구현 어댑터. */
@Component
@RequiredArgsConstructor
public class NicknameWordAdapter implements NicknameWordPort {

    private final NicknameWordRepository nicknameWordRepository;

    @Override
    public List<String> findActiveWordsByType(NicknameWordType type) {
        return nicknameWordRepository.findAllByTypeAndIsActiveTrue(type).stream()
                .map(NicknameWord::getWord)
                .toList();
    }
}
