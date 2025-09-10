package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.furniture.AdminFurnitureRequestDTO;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "furnitures", indexes = {
        @Index(name = "idx_furniture_name_kr", columnList = "furniture_name_kr"),
})
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

    @OneToMany(mappedBy = "furniture")
    private List<FurnitureTag> furnitureTags = new ArrayList<>();

    public void setFurnitureTags(List<FurnitureTag> furnitureTags) {
        this.furnitureTags = furnitureTags;
    }


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
