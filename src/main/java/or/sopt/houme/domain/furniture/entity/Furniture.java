package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.furniture.AdminFurnitureRequestDTO;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "furnitures")
public class Furniture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 가구 이름 (영어)
    @Column(name = "furniture_name_eng", nullable = false)
    private String furnitureNameEng;

    // 가구 이름 (한글)
    @Column(name = "furniture_name_kr", nullable = false, unique = true)
    private String furnitureNameKr;

    // 가구 타입
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_type_id", nullable = false)
    private FurnitureType furnitureType;

    public static Furniture createByAdminFurnitureRequestDTO(AdminFurnitureRequestDTO dto, FurnitureType furnitureType){
        return Furniture.builder()
                .furnitureNameKr(dto.furnitureNameKr())
                .furnitureNameEng(dto.furnitureNameEng())
                .furnitureType(furnitureType)
                .build();
    }

    public void updateFurnitureNameEng(String furnitureNameEng) {
        this.furnitureNameEng = furnitureNameEng;
    }
}
