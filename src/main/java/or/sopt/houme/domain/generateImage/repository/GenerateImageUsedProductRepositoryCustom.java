package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;

import java.util.List;

public interface GenerateImageUsedProductRepositoryCustom {

    List<GenerateImageUsedProduct> findAllByGenerateImageIdInWithRawProduct(List<Long> generateImageIds);
}
