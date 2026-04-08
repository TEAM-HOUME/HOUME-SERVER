package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "furniture_types")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FurnitureType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리 한글 이름
    @Column(name = "name_kr", nullable = false, unique = true)
    private String nameKr;

    // 카테고리 영어 이름
    @Column(name = "name_eng", nullable = false, unique = true)
    private String nameEng;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "priority")
    private Integer priority;

    public void updateFurnitureType(String nameKr, String nameEng) {
        if (nameKr != null && !nameKr.isBlank()){
            this.nameKr = nameKr;
        }
        if (nameEng != null && !nameEng.isBlank()){
            this.nameEng = nameEng;
        }
    }
}
