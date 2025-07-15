package or.sopt.houme.domain.taste.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.repository.taste_tag.TasteTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TasteTagServiceImpl implements TasteTagService {

    private final TasteTagRepository tasteTagRepository;

    // 우선순위 가장 높은 TasteId 반환
    @Override
    public Long getPriorityId(List<Long> ids) {

        Long bestTasteId = tasteTagRepository.findBestTasteId(ids);

        return bestTasteId;
    }
}
