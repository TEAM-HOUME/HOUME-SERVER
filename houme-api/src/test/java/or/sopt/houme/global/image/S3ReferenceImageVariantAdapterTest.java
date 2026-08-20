package or.sopt.houme.global.image;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class S3ReferenceImageVariantAdapterTest {

    @Mock
    private AmazonS3 amazonS3;

    private final VariantKeyResolver variantKeyResolver = new VariantKeyResolver();

    @Test
    @DisplayName("sweep 대상 S3 원본에 1280 WebP variant가 있으면 variant URL을 반환한다")
    void returnsVariantUrlWhenVariantExists() throws MalformedURLException {
        S3ReferenceImageVariantAdapter adapter = new S3ReferenceImageVariantAdapter(
                amazonS3, variantKeyResolver, "houme-bucket", 1280
        );
        String originalUrl = "https://houme-bucket.s3.ap-northeast-2.amazonaws.com/floorplan/room.png";
        String variantKey = "floorplan/room__w1280.webp";
        String variantUrl = "https://houme-bucket.s3.ap-northeast-2.amazonaws.com/" + variantKey;
        given(amazonS3.doesObjectExist("houme-bucket", variantKey)).willReturn(true);
        given(amazonS3.getUrl("houme-bucket", variantKey)).willReturn(new java.net.URL(variantUrl));

        assertThat(adapter.findVariantUrl(originalUrl)).contains(variantUrl);
    }

    @Test
    @DisplayName("외부 상품 URL은 S3 variant를 조회하지 않고 빈 결과를 반환한다")
    void returnsEmptyForExternalProductUrl() {
        S3ReferenceImageVariantAdapter adapter = new S3ReferenceImageVariantAdapter(
                amazonS3, variantKeyResolver, "houme-bucket", 1280
        );

        assertThat(adapter.findVariantUrl("https://example.com/product.jpg")).isEmpty();
        verifyNoInteractions(amazonS3);
    }
}
