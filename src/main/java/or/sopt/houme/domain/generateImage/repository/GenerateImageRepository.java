package or.sopt.houme.domain.generateImage.repository;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerateImageRepository extends JpaRepository<GenerateImage, Long>, GenerateImageRepositoryCustom {
    void deleteByHouseId(Long houseId);
    void flush();
}
