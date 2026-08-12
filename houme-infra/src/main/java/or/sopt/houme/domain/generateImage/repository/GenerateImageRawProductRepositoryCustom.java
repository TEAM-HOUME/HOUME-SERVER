package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImageRawProduct;

import java.util.List;

public interface GenerateImageRawProductRepositoryCustom {

    List<GenerateImageRawProduct> findAllByGenerateImageIdWithRawProduct(Long generateImageId);

    List<GenerateImageRawProduct> findAllByGenerateImageIdInWithRawProduct(List<Long> generateImageIds);
}
