package or.sopt.houme.domain.user.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.house.infra.persistence.HouseJpaEntity;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.mapping.HouseTaste;
import or.sopt.houme.tag.infra.persistence.TagJpaEntity;
import or.sopt.houme.taste.infra.persistence.TasteJpaEntity;
import or.sopt.houme.tastetag.infra.persistence.TasteTagJpaEntity;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryDTO;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import or.sopt.houme.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserRepositoryImpl.class, QuerydslConfig.class})
@ActiveProfiles("test")
class UserRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    private UserJpaEntity mockUser;
    private HouseJpaEntity mockHouse;
    private GenerateImage mockGenerateImage;
    private HouseTaste mockHouseTaste;
    private TasteJpaEntity mockTaste;
    private TasteTagJpaEntity mockTasteTag;
    private TagJpaEntity mockTag;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성
        mockUser = UserJpaEntity.builder()
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

        // 2. 태그 2개 생성
        TagJpaEntity tagModern = TagJpaEntity.builder()
                .tagName("모던")
                .tagPrompt("깔끔한 화이트톤의 거실")
                .tagNameKr("모던")
                .priority(1)
                .build();
        em.persist(tagModern);

        TagJpaEntity tagVintage = TagJpaEntity.builder()
                .tagName("빈티지")
                .tagPrompt("따뜻한 느낌의 원목 인테리어")
                .tagNameKr("빈티지")
                .priority(2)
                .build();
        em.persist(tagVintage);

        // 3. Taste 2개 생성 + 각각 Tag 연결
        TasteJpaEntity taste1 = TasteJpaEntity.builder()
                .url("https://example.com/taste1.png")
                .filename("taste1.png")
                .originalFilename("original-taste1.png")
                .fileExtension("png")
                .build();
        em.persist(taste1);

        TasteJpaEntity taste2 = TasteJpaEntity.builder()
                .url("https://example.com/taste2.png")
                .filename("taste2.png")
                .originalFilename("original-taste2.png")
                .fileExtension("png")
                .build();
        em.persist(taste2);

        em.persist(TasteTagJpaEntity.forInsert(taste1.getId(), tagModern.getId()));
        em.persist(TasteTagJpaEntity.forInsert(taste2.getId(), tagVintage.getId()));
        em.persist(TasteTagJpaEntity.forInsert(taste2.getId(), tagModern.getId())); // taste2는 tag 2개

        // 4. HouseJpaEntity 2개 생성
        HouseJpaEntity house1 = HouseJpaEntity.builder()
                .activity(Activity.REMOTE_WORK)
                .userId(mockUser.getId())
                .isValid(true)
                .build();
        em.persist(house1);

        HouseJpaEntity house2 = HouseJpaEntity.builder()
                .activity(Activity.READING)
                .userId(mockUser.getId())
                .isValid(true)
                .build();
        em.persist(house2);

        // 5. HouseTaste로 Taste 연결
        em.persist(HouseTaste.builder().houseId(house1.getId()).tasteId(taste1.getId()).build());
        em.persist(HouseTaste.builder().houseId(house2.getId()).tasteId(taste2.getId()).build());

        // 6. GenerateImage 2개 생성
        GenerateImage generateImage1 = GenerateImage.builder()
                .url("https://example.com/image1.png")
                .filename("image1.png")
                .originalFilename("original-image1.png")
                .fileExtension("png")
                .house(house1)
                .build();
        em.persist(generateImage1);

        GenerateImage generateImage2 = GenerateImage.builder()
                .url("https://example.com/image2.png")
                .filename("image2.png")
                .originalFilename("original-image2.png")
                .fileExtension("png")
                .house(house2)
                .build();
        em.persist(generateImage2);

        em.flush();
        em.clear();
    }

    // 크레딧 잔액 카운트는 credit 도메인으로 이관되어 CreditApiIntegrationTest(HTTP 계약)가 검증한다 (#581).

    @Test
    @DisplayName("유저 ID로 이미지 히스토리 1건 조회 성공 - 여러 이미지 중 하나")
    void findImageHistoryById_Success() {
        // when
        Optional<GenerateImage> result = userRepositoryImpl.findImageHistoryById(mockUser.getId());

        // then
        assertThat(result).isPresent(); // Optional이 존재해야 함

        GenerateImage image = result.get();
        assertThat(image.getUrl()).isIn(
                "https://example.com/image1.png",
                "https://example.com/image2.png"
        );
        assertThat(image.getFilename()).isIn("image1.png", "image2.png");
    }
}
