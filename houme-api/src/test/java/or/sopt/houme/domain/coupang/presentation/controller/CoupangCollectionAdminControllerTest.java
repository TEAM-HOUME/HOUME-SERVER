package or.sopt.houme.domain.coupang.presentation.controller;

import or.sopt.houme.domain.coupang.service.CoupangCollectionBatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CoupangCollectionAdminControllerTest {

    @Test
    @DisplayName("수동 실행 API는 쿠팡 수집 배치를 한 번 실행한다")
    void runsOneCollectionJob() {
        CoupangCollectionBatchService collectionBatchService = mock(CoupangCollectionBatchService.class);
        CoupangCollectionAdminController controller = new CoupangCollectionAdminController(collectionBatchService);

        var response = controller.runCollectionJob();

        verify(collectionBatchService).runOneJob();
        assertThat(response.getBody().data()).isEqualTo("쿠팡 상품 수집 배치 1건 실행이 완료되었습니다.");
    }
}
