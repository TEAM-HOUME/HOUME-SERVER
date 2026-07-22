package or.sopt.houme.house.domain;

import lombok.Getter;
import or.sopt.houme.domain.house.model.entity.enums.Activity;

/**
 * 집 순수 도메인 모델 (#582 12b-2). JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 * 유저·배너는 id(Long)로만 참조한다. enum(Activity)은 순수 타입이라 기존 패키지를 공유한다.
 */
@Getter
public class House {

    private final Long id;
    private Activity activity;
    private final Long userId;
    private final Long bannerId;
    private final boolean isValid;
    private String housePrompt;

    private House(Long id, Activity activity, Long userId, Long bannerId, boolean isValid, String housePrompt) {
        this.id = id;
        this.activity = activity;
        this.userId = userId;
        this.bannerId = bannerId;
        this.isValid = isValid;
        this.housePrompt = housePrompt;
    }

    /** 신규 생성 (아직 영속화 전이므로 id 없음). */
    public static House create(Activity activity, Long userId, Long bannerId, boolean isValid, String housePrompt) {
        return new House(null, activity, userId, bannerId, isValid, housePrompt);
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static House reconstitute(Long id, Activity activity, Long userId, Long bannerId, boolean isValid, String housePrompt) {
        return new House(id, activity, userId, bannerId, isValid, housePrompt);
    }

    // Activity 업데이트 하기
    public void updateActivity(Activity activity) {
        this.activity = activity;
    }

    // 프롬프트 저장하기
    public void updatePrompt(String prompt) {
        this.housePrompt = prompt;
    }
}
