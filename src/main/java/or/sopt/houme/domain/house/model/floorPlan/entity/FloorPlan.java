package or.sopt.houme.domain.house.model.floorPlan.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImages;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.HouseException;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "floor_plans")
public class FloorPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Column(name = "floor_plan_name", nullable = false)
    private String floorPlanName;

    @Enumerated(EnumType.STRING)
    @Column(name = "form", nullable = false)
    private Form form;                      // 구조

    @Enumerated(EnumType.STRING)
    @Column(name = "structure", nullable = false)
    private Structure structure;            // 형태

    @Enumerated(EnumType.STRING)
    @Column(name = "equilibrium")
    private Equilibrium equilibrium;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "forms_json", columnDefinition = "jsonb")
    private String formsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structures_json", columnDefinition = "jsonb")
    private String structuresJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equilibriums_json", columnDefinition = "jsonb")
    private String equilibriumsJson;

    @Column(name = "floor_plan_prompt", columnDefinition = "TEXT", nullable = false)
    private String floorPlanPrompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images_json", columnDefinition = "jsonb")
    private String imagesJson;

    public static FloorPlan create(
            String floorPlanName,
            java.util.List<Form> forms,
            java.util.List<Structure> structures,
            java.util.List<Equilibrium> equilibriums,
            String floorPlanPrompt,
            FloorPlanImages images,
            String imagesJson,
            String formsJson,
            String structuresJson,
            String equilibriumsJson
    ) {
        validateFloorPlanName(floorPlanName);
        validateSelections(forms);
        validateSelections(structures);
        validateSelections(equilibriums);

        FloorPlan floorPlan = FloorPlan.builder()
                .floorPlanName(floorPlanName)
                .form(forms.getFirst())
                .structure(structures.getFirst())
                .equilibrium(equilibriums.getFirst())
                .formsJson(formsJson)
                .structuresJson(structuresJson)
                .equilibriumsJson(equilibriumsJson)
                .floorPlanPrompt(floorPlanPrompt)
                .build();
        floorPlan.applyImages(images, imagesJson);
        return floorPlan;
    }

    public void update(
            String floorPlanName,
            java.util.List<Form> forms,
            java.util.List<Structure> structures,
            java.util.List<Equilibrium> equilibriums,
            String floorPlanPrompt,
            FloorPlanImages images,
            String imagesJson,
            String formsJson,
            String structuresJson,
            String equilibriumsJson
    ) {
        validateFloorPlanName(floorPlanName);
        validateSelections(forms);
        validateSelections(structures);
        validateSelections(equilibriums);

        this.floorPlanName = floorPlanName;
        this.form = forms.getFirst();
        this.structure = structures.getFirst();
        this.equilibrium = equilibriums.getFirst();
        this.formsJson = formsJson;
        this.structuresJson = structuresJson;
        this.equilibriumsJson = equilibriumsJson;
        this.floorPlanPrompt = floorPlanPrompt;
        applyImages(images, imagesJson);
    }

    private void applyImages(FloorPlanImages images, String imagesJson) {
        FloorPlanImageItem representativeImage = images.representative();
        this.url = representativeImage.url();
        this.filename = representativeImage.filename();
        this.originalFilename = representativeImage.originalFilename();
        this.fileExtension = representativeImage.fileExtension();
        this.imagesJson = imagesJson;
    }

    private static void validateFloorPlanName(String floorPlanName) {
        if (floorPlanName == null || floorPlanName.isBlank()) {
            throw new HouseException(ErrorCode.INVALID_FLOOR_PLAN_NAME);
        }
    }

    private static <T> void validateSelections(java.util.List<T> values) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(java.util.Objects::isNull)) {
            throw new HouseException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }
}
