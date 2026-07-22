package or.sopt.houme.house.infra.persistence;

import or.sopt.houme.house.domain.House;

/** 집 영속 엔티티 ↔ 순수 도메인 모델 매퍼. */
final class HouseMapper {

    private HouseMapper() {
    }

    static House toDomain(HouseJpaEntity entity) {
        return House.reconstitute(
                entity.getId(),
                entity.getActivity(),
                entity.getUserId(),
                entity.getBanner() != null ? entity.getBanner().getId() : null,
                entity.isValid(),
                entity.getHousePrompt()
        );
    }
}
