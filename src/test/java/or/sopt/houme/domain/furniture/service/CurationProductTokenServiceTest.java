package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationProductSearchKeyword;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationProductSearchKeywordRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurationProductTokenService 단위 테스트")
class CurationProductTokenServiceTest {

    @InjectMocks
    private CurationProductTokenService curationProductTokenService;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private CurationProductSearchKeywordRepository searchKeywordRepository;

    @Mock
    private CurationProductTokenizer tokenizer;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("refreshTokensForProducts()는 각 상품의 search_tokens를 갱신하고 저장한다")
    void refreshTokensForProducts_updatesAndSaves() {
        // given
        CurationRawProduct product = CurationRawProduct.builder()
                .id(1L)
                .productId(100L)
                .productName("퀸 침대 프레임")
                .brand("이케아")
                .isExposed(true)
                .furnitureTagMappings(new HashSet<>())
                .build();

        given(curationRawProductRepository.findAllByIdWithFurnitureTags(List.of(1L))).willReturn(List.of(product));
        given(searchKeywordRepository.findAllByCurationRawProductIdIn(anyList())).willReturn(List.of());
        given(tokenizer.buildTokens(any(), any(), anyList(), anyList())).willReturn("퀸 침대 프레임 이케아");

        // when
        curationProductTokenService.refreshTokensForProducts(List.of(1L));

        // then
        assertThat(product.getSearchTokens()).isEqualTo("퀸 침대 프레임 이케아");
        verify(curationRawProductRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("refreshTokensForProducts()는 커스텀 키워드를 포함하여 토크나이저를 호출한다")
    void refreshTokensForProducts_passesCustomKeywordsToTokenizer() {
        // given
        CurationRawProduct product = CurationRawProduct.builder()
                .id(1L)
                .productId(100L)
                .productName("침대")
                .isExposed(true)
                .furnitureTagMappings(new HashSet<>())
                .build();

        CurationProductSearchKeyword keyword = CurationProductSearchKeyword.of(product, "1인용침대");

        given(curationRawProductRepository.findAllByIdWithFurnitureTags(List.of(1L))).willReturn(List.of(product));
        given(searchKeywordRepository.findAllByCurationRawProductIdIn(anyList())).willReturn(List.of(keyword));
        given(tokenizer.buildTokens(any(), any(), anyList(), anyList())).willReturn("침대 1인용침대");

        ArgumentCaptor<List<String>> customKeywordsCaptor = ArgumentCaptor.forClass(List.class);

        // when
        curationProductTokenService.refreshTokensForProducts(List.of(1L));

        // then
        verify(tokenizer).buildTokens(any(), any(), anyList(), customKeywordsCaptor.capture());
        assertThat(customKeywordsCaptor.getValue()).contains("1인용침대");
    }

    @Test
    @DisplayName("refreshTokensForProducts()는 빈 리스트 입력 시 아무것도 처리하지 않는다")
    void refreshTokensForProducts_doesNothingForEmptyList() {
        curationProductTokenService.refreshTokensForProducts(List.of());

        verify(curationRawProductRepository, org.mockito.Mockito.never()).findAllByIdWithFurnitureTags(anyList());
    }
}
