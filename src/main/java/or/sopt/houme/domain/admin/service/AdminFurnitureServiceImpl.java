package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.request.AdminFurnitureTypeRequest;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.request.AdminUpdateFurnitureTypeRequest;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.response.AdminFurnitureTypeListResponse;
import or.sopt.houme.domain.admin.controller.dto.furniture.type.response.AdminFurnitureTypeResponse;
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
import or.sopt.houme.global.api.handler.AdminException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminFurnitureServiceImpl implements AdminFurnitureService {

    private final FurnitureRepository furnitureRepository;
    private final FurnitureTagRepository furnitureTagRepository;
    private final FurnitureTypeRepository furnitureTypeRepository;
    private final TagRepository tagRepository;
    private final S3PresignedUtil s3PresignedUtil;


    /**
     * 신규 가구를 등록하는 메서드입니다.
     *
     * @throws GeneralException 이미 존재하는 가구를 등록하면 예외 발생
     * @throws DataIntegrityViolationException flush 시점에 데이터가 중복되면 예외 발생
     * */
    @Override
    public void registerFurniture(AdminFurnitureRequestDTO dto) {

        FurnitureType furnitureType;

        Optional<Furniture> byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr());
        if (byFurnitureNameKr.isPresent()) {
            throw new GeneralException(ErrorCode.ALREADY_EXIST_FURNITURE);
        }

        furnitureType = furnitureTypeRepository.findById(dto.furnitureType())
                .orElseThrow(()-> new AdminException(ErrorCode.NOT_FOUND_FURNITURE_TYPE));

        Furniture newFurniture = Furniture.createByAdminFurnitureRequestDTO(dto, furnitureType);

        try {
            furnitureRepository.save(newFurniture);
        }catch (DataIntegrityViolationException e){
            throw new GeneralException(ErrorCode.ALREADY_EXIST_FURNITURE);
        }

    }


    /**
     * 가구의 스타일 태그와 프롬프트를 등록하는 메서드입니다
     *
     * @throws GeneralException 입력받은 스타일 태그를 찾지 못했을때 예외 발생
     * @throws GeneralException 입력받은 가구를 찾지 못했을때 예외 발생
     * */
    @Override
    public AdminFurniturePromptCreateResponseDTO registerFurniturePrompt(AdminFurniturePromptRequestDTO dto, String contentType){


        Tag byIdTag = tagRepository.findById(dto.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));


        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));;

        S3PresignedUrlResponseDTO presignedUrl;

        try {
            presignedUrl = s3PresignedUtil.createPresignedUrl(dto.imageExtension(), "furniture", contentType);
        }catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        }

        FurnitureTag newFurnitureTage = FurnitureTag.createByAdminFurniturePromptRequestDTO(
                dto,
                byFurnitureNameKr,
                byIdTag,
                presignedUrl.publicUrl(),
                dto.searchKeyword(),
                dto.priority()
        );

        FurnitureTag saved = furnitureTagRepository.save(newFurnitureTage);

        return new AdminFurniturePromptCreateResponseDTO(presignedUrl.uploadUrl(), saved.getId());

    }


    /**
     * 가구 정보를 모두 조회하는 메서드입니다
     *
     * 09/09 현재 로직이 다소 복잡하여 어떻게 책임을 분리하고 N+1을 해결 할 수 있을지 고민해봐야 할 것 같습니다
     * */
    @Override
    public AdminFurnitureGetDTO getFurniture(){

        List<Furniture> allFurnitures = furnitureRepository.findAllWithTags();

        List<AdminFurnitureGetDTO.FurnitureInfo> furnitureInfos = allFurnitures.stream()
                .map(furniture -> {
                    List<AdminFurnitureGetDTO.TagInfo> tagInfos = furniture.getFurnitureTags().stream()
                            .map(furnitureTag -> new AdminFurnitureGetDTO.TagInfo(
                                    furnitureTag.getId(),
                                    furnitureTag.getTag().getId(),
                                    furnitureTag.getTag().getTagNameKr(),
                                    furnitureTag.getFurnitureUrl(),
                                    furnitureTag.getSearchKeyword(),
                                    furnitureTag.getPriority()
                            ))
                            .toList();

                    return new AdminFurnitureGetDTO.FurnitureInfo(
                            furniture.getId(),
                            furniture.getFurnitureNameKr(),
                            tagInfos);
                })
                .toList();

        return new AdminFurnitureGetDTO(furnitureInfos);
    }


    /**
     * 가구와 그에 맞는 태그를 조회하는 메서드입니다
     * */
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


    /**
     * 가구 정보를 업데이트하는 메서드입니다
     * 현재 업데이트 가능한 정보는 가구의 영어명과 프롬프트입니다
     *
     * @throws GeneralException 가구정보를 찾을 수 없을때 예외 발생
     * @throws GeneralException 태그정보를 찾을 수 없을때 예외 발생
     * @throws GeneralException 가구와 태그의 매핑테이블 엔티티 정보를 찾을 수 없을때 예외 발생
     * */
    @Override
    public AdminFurnitureUpdateResponseDTO updateFurniture(AdminFurnitureUpdateRequestDTO dto, String contentType){

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

        if (dto.newSearchKeyword() != null && !dto.newSearchKeyword().isBlank()){
            byFurnitureIdAndTag.updateSearchKeyword(dto.newSearchKeyword());
        }

        if (dto.newPriority() != null){
            byFurnitureIdAndTag.updatePriority(dto.newPriority());
        }

        // 이미지 업데이트 요청이 있는 경우 presigned URL 발급 및 publicUrl 갱신
        if (dto.imageExtension() != null && !dto.imageExtension().isBlank() && contentType != null && !contentType.isBlank()) {
            S3PresignedUrlResponseDTO presignedUrl;
            try {
                presignedUrl = s3PresignedUtil.createPresignedUrl(dto.imageExtension(), "furniture", contentType);
            } catch (Exception e) {
                throw new GeneralException(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
            }
            byFurnitureIdAndTag.updateFurnitureUrl(presignedUrl.publicUrl());
            return AdminFurnitureUpdateResponseDTO.of(presignedUrl.uploadUrl(), byFurnitureIdAndTag.getId());
        }

        return AdminFurnitureUpdateResponseDTO.of(null, byFurnitureIdAndTag.getId());
    }


    /**
     * 가구와 태그의 매핑 정보를 삭제하는 메서드입니다
     *
     * @throws GeneralException 가구정보를 찾을 수 없을때 예외 발생
     * @throws GeneralException 태그정보를 찾을 수 없을때 예외 발생
     * @throws GeneralException 가구와 태그의 매핑테이블 엔티티 정보를 찾을 수 없을때 예외 발생
     * @throws DataIntegrityViolationException 연관된 데이터가 존재하는 경우 예외 발생
     * */
    @Override
    public void deleteFurnitureTag(AdminFurnitureTagDeleteDTO dto){

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        Tag byIdTag = tagRepository.findById(dto.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        FurnitureTag byFurnitureIdAndTag = furnitureTagRepository.findByFurnitureAndTag(byFurnitureNameKr, byIdTag)
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));

        try {
            furnitureTagRepository.delete(byFurnitureIdAndTag);
        }catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL);
        }

    }


    /**
     * 가구 데이터를 삭제하는 메서드입니다
     *
     * @throws GeneralException 가구 정보를 찾을 수 없는 경우 예외 발생
     * @throws GeneralException 어플리케이션 단에서 가구의 매핑 데이터로 인해서 데이터를 삭제 할 수 없는 경우 예외 발생
     * @throws GeneralException DB 단에서 가구의 매핑 데이터로 인해서 데이터를 삭제 할 수 없는 경우 예외 발생
     * */
    @Override
    public void deleteFurniture(AdminFurnitureDeleteDTO dto){

        Furniture byFurnitureNameKr = furnitureRepository.findByFurnitureNameKr(dto.furnitureNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE));

        List<FurnitureTag> furnitureTags = furnitureTagRepository.findByFurniture(byFurnitureNameKr);
        if (!furnitureTags.isEmpty()) {
            throw new GeneralException(ErrorCode.INVALID_DELETE_FURNITURE);
        }

        try {
            furnitureRepository.delete(byFurnitureNameKr);
        }catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL);
        }
    }


    /**
     * 가구의 상세정보를 조회하는 메서드입니다
     *
     * @throws GeneralException 가구를 찾을 수 없는 경우 예외 발생
     * @throws GeneralException 태그를 찾을 수 없는 경우 예외 발생
     * @throws GeneralException 가구와 태그의 매핑테이블 엔티티 정보를 찾을 수 없을때 예외 발생
     * */
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

    /**
     * FurnitureType 반환 메서드
     * @return AdminFurnitureTypeListResponse
     */
    @Override
    public AdminFurnitureTypeListResponse getFurnitureTypes() {

        List<AdminFurnitureTypeResponse> list = furnitureTypeRepository.findAll().stream()
                .map(AdminFurnitureTypeResponse::of)
                .toList();

        return new AdminFurnitureTypeListResponse(list);
    }

    /**
     * FurnitureType 등록 메서드
     */
    @Override
    public void registerFurnitureType(AdminFurnitureTypeRequest request) {

        // 한글명이 이미 있는지 확인
        if (furnitureTypeRepository.existsByNameKr(request.furnitureTypeNameKr())) {
            throw new AdminException(ErrorCode.DUPLICATE_FURNITURE_TYPE_KR);
        }

        // 영어명이 이미 있는지 확인
        if (furnitureTypeRepository.existsByNameEng(request.furnitureTypeNameEng().toUpperCase())) {
            throw new AdminException(ErrorCode.DUPLICATE_FURNITURE_TYPE_ENG);
        }

        // 가구타입 등록
        FurnitureType entity = FurnitureType.builder()
                .nameKr(request.furnitureTypeNameKr())
                .nameEng(request.furnitureTypeNameEng().toUpperCase())
                .build();

        furnitureTypeRepository.save(entity);
    }

    /**
     * FurnitureType 삭제 메서드
     */
    @Override
    public void deleteFurnitureType(long furnitureTypeId) {

        FurnitureType type = furnitureTypeRepository.findById(furnitureTypeId)
                .orElseThrow(() -> new AdminException(ErrorCode.NOT_FOUND_FURNITURE_TYPE));

        boolean hasFurnitures = furnitureRepository.existsByFurnitureType(type);
        if (hasFurnitures) {
            throw new AdminException(ErrorCode.CANNOT_DELETE_FURNITURE_TYPE_IN_USE);
        }

        furnitureTypeRepository.delete(type);
    }

    /**
     * FurnitureType 수정 메서드
     */
    @Override
    public void updateFurnitureType(AdminUpdateFurnitureTypeRequest request) {

        FurnitureType furnitureType = furnitureTypeRepository.findById(request.id())
                .orElseThrow(() -> new AdminException(ErrorCode.NOT_FOUND_FURNITURE_TYPE));

        // 한글명이 이미 있는지 확인
        if (furnitureTypeRepository.existsByNameKr(request.furnitureTypeNameKr())) {
            throw new AdminException(ErrorCode.DUPLICATE_FURNITURE_TYPE_KR);
        }

        // 영어명이 이미 있는지 확인
        if (furnitureTypeRepository.existsByNameEng(request.furnitureTypeNameEng().toUpperCase())) {
            throw new AdminException(ErrorCode.DUPLICATE_FURNITURE_TYPE_ENG);
        }

        furnitureType.updateFurnitureType(request.furnitureTypeNameKr(), request.furnitureTypeNameEng().toUpperCase());
    }
}
