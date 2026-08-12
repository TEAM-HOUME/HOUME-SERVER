package or.sopt.houme.furniture.domain;

import lombok.Getter;

/**
 * 가구 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 *
 * <p>영속 엔티티({@link or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity})와 분리된 도메인 전용 타입이다.
 * 가구 타입은 {@code @ManyToOne} 대신 furnitureTypeId(Long)로만 참조해 도메인 경계를 유지한다(FK 는 DB 가 계속 강제).
 * furniture 도메인 내부의 그래프 순회(FurnitureTag/Curation 등)는 여전히 {@code FurnitureJpaEntity} 를 사용하며,
 * 이 순수 모델은 도메인 경계를 넘는 소비처(House/User 등)에 반환하는 용도다.
 */
@Getter
public class Furniture {

    private final Long id;
    private final String furnitureNameEng;
    private final String furnitureNameKr;
    private final Long furnitureTypeId;
    private final String object365Word;
    private final Integer priority;

    private Furniture(Long id, String furnitureNameEng, String furnitureNameKr,
                      Long furnitureTypeId, String object365Word, Integer priority) {
        this.id = id;
        this.furnitureNameEng = furnitureNameEng;
        this.furnitureNameKr = furnitureNameKr;
        this.furnitureTypeId = furnitureTypeId;
        this.object365Word = object365Word;
        this.priority = priority;
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static Furniture reconstitute(Long id, String furnitureNameEng, String furnitureNameKr,
                                         Long furnitureTypeId, String object365Word, Integer priority) {
        return new Furniture(id, furnitureNameEng, furnitureNameKr, furnitureTypeId, object365Word, priority);
    }
}
