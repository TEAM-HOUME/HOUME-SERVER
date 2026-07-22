package or.sopt.houme.furniture.domain;

import lombok.Getter;

/**
 * 찜(jjym) 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 *
 * <p>유저·추천가구는 id(Long)로만 참조한다. 낙관락 @Version 은 영속 계층 관심사라 도메인에 두지 않는다.
 */
@Getter
public class Jjym {

    private final Long id;
    private final Long userId;
    private final Long recommendFurnitureId;

    private Jjym(Long id, Long userId, Long recommendFurnitureId) {
        this.id = id;
        this.userId = userId;
        this.recommendFurnitureId = recommendFurnitureId;
    }

    /** 신규 찜 (아직 영속화 전이므로 id 없음). */
    public static Jjym of(Long userId, Long recommendFurnitureId) {
        return new Jjym(null, userId, recommendFurnitureId);
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static Jjym reconstitute(Long id, Long userId, Long recommendFurnitureId) {
        return new Jjym(id, userId, recommendFurnitureId);
    }
}
