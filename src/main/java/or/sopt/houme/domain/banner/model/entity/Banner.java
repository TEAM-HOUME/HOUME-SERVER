package or.sopt.houme.domain.banner.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "banners")
@Comment("스타일 배너 본체 정보")
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "banner_type", length = 20)
    @Comment("콘텐츠 타입(BANNER, STYLE)")
    private BannerType bannerType;

    @Column(name = "banner_image_url", nullable = false, length = 2048)
    @Comment("배너 대표 이미지 URL")
    private String bannerImageUrl;

    @Column(name = "banner_title", nullable = false)
    @Comment("배너 타이틀")
    private String bannerTitle;

    @Column(name = "style_description", columnDefinition = "TEXT")
    @Comment("스타일 설명")
    private String styleDescription;

    @Column(name = "style_question", columnDefinition = "TEXT")
    @Comment("스타일 질문")
    private String styleQuestion;

    @Column(name = "style_prompt", columnDefinition = "TEXT")
    @Comment("이미지 생성용 스타일 프롬프트")
    private String stylePrompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "style_answer_chips_json", columnDefinition = "jsonb")
    @Comment("스타일 답변 칩 목록 JSON")
    private String styleAnswerChipsJson;

    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BannerCurationRawProduct> bannerRawProducts = new ArrayList<>();

    public static Banner create(
            BannerType bannerType,
            String bannerImageUrl,
            String bannerTitle,
            String styleDescription,
            String styleQuestion,
            String stylePrompt,
            String styleAnswerChipsJson
    ) {
        return Banner.builder()
                .bannerType(bannerType)
                .bannerImageUrl(bannerImageUrl)
                .bannerTitle(bannerTitle)
                .styleDescription(styleDescription)
                .styleQuestion(styleQuestion)
                .stylePrompt(stylePrompt)
                .styleAnswerChipsJson(styleAnswerChipsJson)
                .build();
    }

    public void update(
            BannerType bannerType,
            String bannerImageUrl,
            String bannerTitle,
            String styleDescription,
            String styleQuestion,
            String stylePrompt,
            String styleAnswerChipsJson
    ) {
        this.bannerType = bannerType;
        this.bannerImageUrl = bannerImageUrl;
        this.bannerTitle = bannerTitle;
        this.styleDescription = styleDescription;
        this.styleQuestion = styleQuestion;
        this.stylePrompt = stylePrompt;
        this.styleAnswerChipsJson = styleAnswerChipsJson;
    }

    public void replaceRawProducts(List<BannerCurationRawProduct> mappings) {
        List<BannerCurationRawProduct> safeMappings = mappings == null ? List.of() : mappings;
        Map<Long, BannerCurationRawProduct> existingByRawProductId = this.bannerRawProducts.stream()
                .collect(Collectors.toMap(
                        Banner::extractRawProductId,
                        Function.identity(),
                        (left, right) -> left
                ));
        Set<Long> incomingRawProductIds = safeMappings.stream()
                .map(Banner::extractRawProductId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        this.bannerRawProducts.removeIf(mapping -> !incomingRawProductIds.contains(extractRawProductId(mapping)));

        safeMappings.stream()
                .map(Banner::extractRawProduct)
                .filter(rawProduct -> !existingByRawProductId.containsKey(rawProduct.getId()))
                .forEach(rawProduct -> this.bannerRawProducts.add(BannerCurationRawProduct.of(this, rawProduct)));
    }

    private static Long extractRawProductId(BannerCurationRawProduct mapping) {
        return extractRawProduct(mapping).getId();
    }

    private static CurationRawProduct extractRawProduct(BannerCurationRawProduct mapping) {
        BannerCurationRawProduct safeMapping = Objects.requireNonNull(mapping, "mapping must not be null");
        return Objects.requireNonNull(safeMapping.getCurationRawProduct(), "curationRawProduct must not be null");
    }
}
