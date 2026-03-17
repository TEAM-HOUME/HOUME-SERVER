package or.sopt.houme.domain.house.model.floorPlan.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImages;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "form", nullable = false)
    private Form form;                      // 구조

    @Enumerated(EnumType.STRING)
    @Column(name = "structure", nullable = false)
    private Structure structure;            // 형태

    @Enumerated(EnumType.STRING)
    @Column(name = "equilibrium")
    private Equilibrium equilibrium;

    @Column(name = "floor_plan_prompt", columnDefinition = "TEXT", nullable = false)
    private String floorPlanPrompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images_json", columnDefinition = "jsonb")
    private String imagesJson;

    public static FloorPlan create(
            Form form,
            Structure structure,
            Equilibrium equilibrium,
            String floorPlanPrompt,
            FloorPlanImages images,
            String imagesJson
    ) {
        FloorPlan floorPlan = FloorPlan.builder()
                .form(form)
                .structure(structure)
                .equilibrium(equilibrium)
                .floorPlanPrompt(floorPlanPrompt)
                .build();
        floorPlan.applyImages(images, imagesJson);
        return floorPlan;
    }

    public void update(
            Form form,
            Structure structure,
            Equilibrium equilibrium,
            String floorPlanPrompt,
            FloorPlanImages images,
            String imagesJson
    ) {
        this.form = form;
        this.structure = structure;
        this.equilibrium = equilibrium;
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
}

