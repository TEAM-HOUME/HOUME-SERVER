package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.request.AdminUpdateFurnitureTypeRequest;

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

    public void updateFurnitureType(AdminUpdateFurnitureTypeRequest request) {
        this.nameKr = request.furnitureTypeNameKr();
        this.nameEng = request.furnitureTypeNameEng();
    }
}
