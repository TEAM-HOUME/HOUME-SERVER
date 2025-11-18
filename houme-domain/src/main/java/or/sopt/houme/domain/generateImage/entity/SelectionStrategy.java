package or.sopt.houme.domain.generateImage.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectionStrategy {
    TOP2_BY_PRIORITY("top2_by_priority"),
    TOP1("top1");

    private final String strategy;
}
