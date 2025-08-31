package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminFurnitureGetDto;
import or.sopt.houme.domain.admin.controller.dto.AdminFurniturePromptRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.AdminFurnitureRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.AdminFurnitureTagGetDTO;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminFurnitureServiceImpl implements AdminFurnitureService {

    private final FurnitureRepository furnitureRepository;
    private final FurnitureTagRepository furnitureTagRepository;
    private final FurnitureTypeRepository furnitureTypeRepository;
    private final TagRepository tagRepository;


    @Override
    public void registerFurniture(AdminFurnitureRequestDTO dto) {

        FurnitureType furnitureType;
        if (dto.isBed()){
            furnitureType = furnitureTypeRepository.findById(1L)
                    .orElseThrow(()-> new GeneralException(ErrorCode.NOT_VALID_EXCEPTION));
        }else {
            furnitureType = furnitureTypeRepository.findById(2L)
                    .orElseThrow(()-> new GeneralException(ErrorCode.NOT_VALID_EXCEPTION));
        }

        Furniture newFurniture = Furniture.builder()
                .furnitureNameKr(dto.furnitureNameKr())
                .furnitureNameEng(dto.furnitureNameEng())
                .furnitureType(furnitureType)
                .build();

        furnitureRepository.save(newFurniture);

    }


    @Override
    public void registerFurniturePrompt(AdminFurniturePromptRequestDTO dto){

        Tag byIdTag = tagRepository.findById(dto.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_VALID_EXCEPTION));

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_VALID_EXCEPTION));;

        FurnitureTag newFurnitureTage = FurnitureTag.builder()
                .furniturePrompt(dto.prompt())
                .furniture(byFurnitureNameKr)
                .tag(byIdTag)
                .build();

        furnitureTagRepository.save(newFurnitureTage);

    }


    @Override
    public AdminFurnitureGetDto getFurniture(){

        List<Furniture> all = furnitureRepository.findAll();
        List<String> furnitureNames = all.stream()
                .map(Furniture::getFurnitureNameKr)
                .toList();

        return new AdminFurnitureGetDto(furnitureNames);
    }


    @Override
    public AdminFurnitureTagGetDTO getFurnitureTag() {
        List<Tag> all = tagRepository.findAll();

        List<Long> tagIds = all.stream()
                .map(Tag::getId)
                .toList();

        List<String> tagNames = all.stream()
                .map(Tag::getTagNameKr)
                .toList();

        return new AdminFurnitureTagGetDTO(tagIds, tagNames);
    }
}
