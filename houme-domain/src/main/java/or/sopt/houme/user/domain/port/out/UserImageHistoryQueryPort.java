package or.sopt.houme.user.domain.port.out;

import or.sopt.houme.domain.user.presentation.controller.dto.ImageHistoriesResultPageResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryListResponse;

/**
 * 마이페이지 이미지 히스토리 조회 아웃바운드 포트 (#582).
 *
 * <p>house·generateImage·banner·curation 엔티티 그래프를 넘나드는 무거운 read 조립을
 * infra 어댑터로 내려, 애플리케이션(UserService)은 이 포트만 소비한다.
 * 반환 DTO 는 엔티티-프리 플레인 레코드라 domain 포트 시그니처에 둘 수 있다(P2 시 common 배정).
 */
public interface UserImageHistoryQueryPort {

    UserImageHistoryListResponse getUserImageHistoryList(Long userId);

    MyPageGeneratedImageV2Response getUserGeneratedImageHistoryListV2(Long userId);

    ImageHistoriesResultPageResponse getImageHistoryResultPage(Long userId, String userDisplayName, Long houseId);
}
