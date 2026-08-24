package or.sopt.houme.coupang.presentation.controller;

import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.coupang.service.CoupangProductSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminCoupangProductControllerTest {

    @Test
    @DisplayName("단건 쿠팡 검색은 배치 저장 없이 파트너스 검색 결과를 응답으로 변환한다")
    void searchProducts() {
        CoupangProductSearchService service = mock(CoupangProductSearchService.class);
        when(service.search(eq("3인용 소파"), eq(10))).thenReturn(List.of(
                new CoupangProductSearchResult(
                        "1", "테스트 소파", new BigDecimal("100000"), "https://image", "https://product"
                )
        ));
        AdminCoupangProductController controller = new AdminCoupangProductController(service);

        var response = controller.searchProducts("3인용 소파", 10);

        assertThat(response.getBody().data()).singleElement()
                .satisfies(product -> {
                    assertThat(product.productId()).isEqualTo("1");
                    assertThat(product.price()).isEqualByComparingTo("100000");
                });
    }
}
