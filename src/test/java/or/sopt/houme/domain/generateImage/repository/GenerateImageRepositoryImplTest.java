package or.sopt.houme.domain.generateImage.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({GenerateImageRepositoryImpl.class, QuerydslConfig.class})
@ActiveProfiles("test")
class GenerateImageRepositoryImplTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private GenerateImageRepositoryImpl generateImageRepositoryImpl;

    private User user;
    private House house;
    private GenerateImage generateImage;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("테스트유저")
                .email("test+" + UUID.randomUUID() + "@email.com")
                .password("1234")
                .birthday(LocalDate.of(1995, 5, 5))
                .gender(Gender.FEMALE)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();
        em.persist(user);

        house = House.builder()
                .user(user)
                .activity(Activity.REMOTE_WORK)
                .isValid(true)
                .build();
        em.persist(house);

        generateImage = GenerateImage.builder()
                .url("https://cdn.com/image.png")
                .filename("image.png")
                .originalFilename("origin-image.png")
                .fileExtension("png")
                .house(house)
                .build();
        em.persist(generateImage);

        em.flush();
        em.clear();

        // H2 + PostgreSQLDialect 조합에서 jsonb 컬럼을 가진 일부 테이블이 자동 생성되지 않는 경우를 보완
        em.createNativeQuery("create table if not exists banners (id bigint primary key)").executeUpdate();
        em.createNativeQuery("""
                create table if not exists banner_curation_raw_products (
                    id bigint auto_increment primary key,
                    banner_id bigint not null,
                    curation_raw_product_id bigint not null
                )
                """).executeUpdate();
    }

    @Test
    @DisplayName("findMostRecentByUserId는 유저 기준 최신 생성 이미지를 반환한다")
    void findMostRecentByUserId_returnsLatestImage() {
        Optional<GenerateImage> result = generateImageRepositoryImpl.findMostRecentByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(generateImage.getId());
    }

    @Test
    @DisplayName("findRelatedImagesByRawProductIds는 현재 이미지 제외, 중복 제거, 최신순 정렬을 보장한다")
    void findRelatedImagesByRawProductIds_excludesCurrent_deduplicates_ordersLatest() {
        CurationRawProduct targetRawProduct = CurationRawProduct.builder()
                .source("repo_test")
                .category(SoozipCategory.FURNITURE)
                .productId(101L)
                .productImageUrl("https://cdn.com/raw-101.png")
                .productSiteUrl("https://shop.com/raw-101")
                .productName("타겟 상품")
                .productMallName("몰")
                .fetchedAt(LocalDateTime.now())
                .isExposed(true)
                .build();
        em.persist(targetRawProduct);

        CurationRawProduct otherRawProduct = CurationRawProduct.builder()
                .source("repo_test")
                .category(SoozipCategory.FURNITURE)
                .productId(202L)
                .productImageUrl("https://cdn.com/raw-202.png")
                .productSiteUrl("https://shop.com/raw-202")
                .productName("비대상 상품")
                .productMallName("몰")
                .fetchedAt(LocalDateTime.now())
                .isExposed(true)
                .build();
        em.persist(otherRawProduct);

        GenerateImage current = GenerateImage.builder()
                .url("https://cdn.com/current.png")
                .filename("current.png")
                .originalFilename("current-origin.png")
                .fileExtension("png")
                .house(house)
                .generationType(GenerateImageType.BANNER)
                .build();
        em.persist(current);

        GenerateImage related1 = GenerateImage.builder()
                .url("https://cdn.com/related1.png")
                .filename("related1.png")
                .originalFilename("related1-origin.png")
                .fileExtension("png")
                .house(house)
                .generationType(GenerateImageType.BANNER)
                .build();
        em.persist(related1);

        GenerateImage related2 = GenerateImage.builder()
                .url("https://cdn.com/related2.png")
                .filename("related2.png")
                .originalFilename("related2-origin.png")
                .fileExtension("png")
                .house(house)
                .generationType(GenerateImageType.FULL_FUNNEL)
                .build();
        em.persist(related2);

        House nonMatchedHouse = House.builder()
                .user(user)
                .activity(Activity.REMOTE_WORK)
                .isValid(true)
                .build();
        em.persist(nonMatchedHouse);

        GenerateImage nonMatched = GenerateImage.builder()
                .url("https://cdn.com/non-matched.png")
                .filename("non-matched.png")
                .originalFilename("non-matched-origin.png")
                .fileExtension("png")
                .house(nonMatchedHouse)
                .generationType(GenerateImageType.BANNER)
                .build();
        em.persist(nonMatched);

        // 배너 + 배너 매핑(native) : house가 배너를 바라보도록 구성
        em.createNativeQuery("insert into banners(id) values (100)").executeUpdate();
        em.createNativeQuery("update houses set banner_id = 100 where id = :houseId")
                .setParameter("houseId", house.getId())
                .executeUpdate();
        em.createNativeQuery("update generate_images set generation_type = 'RECOMMEND' where id = :seedImageId")
                .setParameter("seedImageId", generateImage.getId())
                .executeUpdate();
        em.createNativeQuery("""
                insert into banner_curation_raw_products(banner_id, curation_raw_product_id)
                values (100, :rawProductId)
                """)
                .setParameter("rawProductId", targetRawProduct.getId())
                .executeUpdate();

        // related1 은 used product로도 동일 상품을 한번 더 연결(중복 후보 시나리오)
        em.persist(GenerateImageUsedProduct.of(related1, targetRawProduct, 1));
        // related2 는 used product로 targetRawProduct와 연결
        em.persist(GenerateImageUsedProduct.of(related2, targetRawProduct, 1));
        // nonMatched 는 다른 상품 연결
        em.persist(GenerateImageUsedProduct.of(nonMatched, otherRawProduct, 1));

        em.flush();
        em.clear();

        List<GenerateImage> result = generateImageRepositoryImpl.findRelatedImagesByRawProductIds(
                List.of(targetRawProduct.getId()),
                current.getId(),
                10,
                Set.of(GenerateImageType.BANNER, GenerateImageType.STYLE, GenerateImageType.PRODUCT)
        );

        assertThat(result).extracting(GenerateImage::getId).doesNotContain(current.getId());
        assertThat(result).extracting(GenerateImage::getId).doesNotHaveDuplicates();
        assertThat(result).extracting(GenerateImage::getId).containsExactly(related1.getId());

        List<GenerateImage> emptyTypeResult = generateImageRepositoryImpl.findRelatedImagesByRawProductIds(
                List.of(targetRawProduct.getId()),
                current.getId(),
                10,
                Set.of()
        );

        assertThat(emptyTypeResult).isEmpty();
    }
}
