package or.sopt.houme.domain.generateImage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.taste.entity.Tag;

// 선정된 태그와 선정 방식을 담을 클래스
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SelectedTagInfo {
    private Tag tag;
    private String selectionStrategy; // "가장 많은 태그", "동률 시 우선순위"

}
