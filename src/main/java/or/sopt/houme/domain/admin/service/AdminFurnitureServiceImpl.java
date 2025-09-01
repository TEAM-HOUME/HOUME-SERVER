package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.furniture.*;
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
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));;

        FurnitureTag newFurnitureTage = FurnitureTag.builder()
                .furniturePrompt(dto.prompt())
                .furniture(byFurnitureNameKr)
                .tag(byIdTag)
                .build();

        furnitureTagRepository.save(newFurnitureTage);

    }


    @Override
    public AdminFurnitureGetDTO getFurniture(){

        List<Furniture> allFurnitures = furnitureRepository.findAll();

        List<AdminFurnitureGetDTO.FurnitureInfo> furnitureInfos = allFurnitures.stream()
                .map(furniture -> {
                    List<FurnitureTag> furnitureTags = furnitureTagRepository.findByFurniture(furniture);
                    List<AdminFurnitureGetDTO.TagInfo> tagInfos = furnitureTags.stream()
                            .map(furnitureTag -> new AdminFurnitureGetDTO.TagInfo(
                                    furnitureTag.getTag().getId(),
                                    furnitureTag.getTag().getTagNameKr()))
                            .toList();

                    return new AdminFurnitureGetDTO.FurnitureInfo(
                            furniture.getId(),
                            furniture.getFurnitureNameKr(),
                            tagInfos);
                })
                .toList();

        return new AdminFurnitureGetDTO(furnitureInfos);
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


    @Override
    public void updateFurniture(AdminFurnitureUpdateRequestDTO dto){

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Tag byIdTag = tagRepository.findById(dto.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        FurnitureTag byFurnitureIdAndTag = furnitureTagRepository.findByFurnitureAndTag(byFurnitureNameKr, byIdTag)
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));

        if (dto.newFurnitureNameEng() != null && !dto.newFurnitureNameEng().isBlank()){
            byFurnitureNameKr.updateFurnitureNameEng(dto.newFurnitureNameEng());
        }

        if (dto.newPrompt() != null && !dto.newPrompt().isBlank()){
            byFurnitureIdAndTag.updatePrompt(dto.newPrompt());
        }
    }


    @Override
    public void deleteFurnitureTag(AdminFurnitureTagDeleteDTO dto){

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Tag byIdTag = tagRepository.findById(dto.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        FurnitureTag byFurnitureIdAndTag = furnitureTagRepository.findByFurnitureAndTag(byFurnitureNameKr, byIdTag)
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));

        furnitureTagRepository.delete(byFurnitureIdAndTag);

    }


    @Override
    public void deleteFurniture(AdminFurnitureDeleteDTO dto){

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        List<FurnitureTag> furnitureTags = furnitureTagRepository.findByFurniture(byFurnitureNameKr);
        if (!furnitureTags.isEmpty()) {
            throw new GeneralException(ErrorCode.INVALID_DELETE_FURNITURE);
        }

        furnitureRepository.delete(byFurnitureNameKr);
    }


    @Override
    public AdminFurnitureDetailsResponseDTO getDetails(AdminFurnitureDetailsRequestDTO dto){

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Tag byIdTag = tagRepository.findById(dto.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        FurnitureTag byFurnitureIdAndTag = furnitureTagRepository.findByFurnitureAndTag(byFurnitureNameKr, byIdTag)
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));

        String furniturePrompt = byFurnitureIdAndTag.getFurniturePrompt();

        return new AdminFurnitureDetailsResponseDTO(furniturePrompt);
    }
}