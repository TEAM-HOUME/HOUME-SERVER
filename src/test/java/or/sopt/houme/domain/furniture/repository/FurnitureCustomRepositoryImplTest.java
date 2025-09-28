package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.entity.mapping.HouseFurniture;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({FurnitureCustomRepositoryImpl.class, QuerydslConfig.class}) // QueryDSL 구현체 직접 등록
class FurnitureRepositoryImplTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    FurnitureRepository furnitureRepository;

    @Autowired
    JPAQueryFactory queryFactory;

    @Test
    @DisplayName("houseId로 매핑된 가구들을 조회할 수 있다.")
    void findAllByHouseId_success() {
        // given
        User mockUser = User.builder()
                .name("테스트유저")
                .birthday(LocalDate.of(1999, 12, 31))
                .gender(Gender.MALE)
                .email("mock@example.com")
                .password("encodedPassword123")
                .hasGeneratedImage(true)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();
        em.persist(mockUser);

        FurnitureType bedType = FurnitureType.builder()
                .isRequired(true)
                .build();

        em.persist(bedType);

        Furniture bed = Furniture.builder()
                .furnitureNameEng("Bed")
                .furnitureNameKr("침대")
                .furnitureType(bedType)
                .build();

        Furniture desk = Furniture.builder()
                .furnitureNameEng("Desk")
                .furnitureNameKr("책상")
                .furnitureType(bedType)
                .build();

        em.persist(bed);
        em.persist(desk);

        House house = House.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .activity(Activity.REMOTE_WORK)
                .user(mockUser)
                .isValid(true)
                .build();

        em.persist(house);

        HouseFurniture hf1 = HouseFurniture.builder()
                .house(house)
                .furniture(bed)
                .build();

        HouseFurniture hf2 = HouseFurniture.builder()
                .house(house)
                .furniture(desk)
                .build();

        em.persist(hf1);
        em.persist(hf2);

        em.flush();
        em.clear();

        // when
        List<Furniture> result = furnitureRepository.findAllByHouseId(house.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Furniture::getFurnitureNameEng)
                .containsExactlyInAnyOrder("Bed", "Desk");
    }
}
