package or.sopt.houme.global.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.api.handler.S3Exception;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("S3Util Class Test")
class S3UtilImplTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3UtilImpl s3Util;

    private final String bucket = "test-bucket";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(s3Util, "bucket", bucket);
    }


    @Test
    @DisplayName("upload()를 통해 multipartFile 타입의 이미지를 S3에 업로드 할 수 있다")
    void upload_ShouldReturnImageUploadResponseDTO() throws Exception {
        String dirName = "test";
        String fileName = "test.png";
        byte[] content = "mock image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("http://mock-url.com"));

        ImageUploadResponseDTO response = s3Util.upload(dirName, multipartFile);

        assertNotNull(response);
        assertTrue(response.getFilename().contains(dirName));
        assertEquals(fileName, response.getOriginalFilename());
        assertEquals("http://mock-url.com", response.getImageLink());
    }


    @Test
    @DisplayName("upload() 중 AmazonServiceException 이 발생하면 정해진 예외가 발생한다")
    void upload_ShouldThrowS3Exception_WhenAmazonServiceExceptionOccurs() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("file.png");
        when(multipartFile.getSize()).thenReturn(123L);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        doThrow(new AmazonServiceException("AWS error"))
                .when(amazonS3).putObject(any(PutObjectRequest.class));

        S3Exception exception = assertThrows(S3Exception.class, () ->
                s3Util.upload("dir", multipartFile));
        assertEquals(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION, exception.getErrorCode());
    }


    @Test
    @DisplayName("upload() 중 S3Exception 이 발생하면 정해진 예외가 발생한다")
    void upload_ShouldThrowS3Exception_WhenIOExceptionOccurs() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("file.png");
        when(multipartFile.getSize()).thenReturn(123L);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenThrow(new IOException("Stream error"));

        S3Exception exception = assertThrows(S3Exception.class, () ->
                s3Util.upload("dir", multipartFile));
        assertEquals(ErrorCode.IMAGE_UPLOAD_IO_EXCEPTION, exception.getErrorCode());
    }


    @Test
    @DisplayName("uploadByte()를 통해 Byte[] 타입의 이미지를 S3에 업로드 할 수 있다")
    void uploadByByte_ShouldReturnImageUploadResponseDTO() throws Exception {
        byte[] imageBytes = "mock image".getBytes();
        String dirName = "images";

        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("http://mock-url.com"));

        ImageUploadResponseDTO response = s3Util.uploadByByte(dirName, imageBytes);

        assertNotNull(response);
        assertTrue(response.getFilename().contains(dirName));
        assertTrue(response.getOriginalFilename().endsWith(".png"));
        assertEquals("http://mock-url.com", response.getImageLink());
    }


    @Test
    @DisplayName("uploadByte() 중 AmazonServiceException 이 발생하면 정해진 예외가 발생한다")
    void uploadByByte_ShouldThrowS3Exception_WhenAmazonServiceExceptionOccurs() {
        byte[] imageBytes = "fake image data".getBytes();

        doThrow(new AmazonServiceException("AWS error"))
                .when(amazonS3).putObject(any(PutObjectRequest.class));

        S3Exception exception = assertThrows(S3Exception.class, () ->
                s3Util.uploadByByte("dir", imageBytes));
        assertEquals(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION, exception.getErrorCode());
    }


    @Test
    @DisplayName("uploadByte() 중 S3Exception 이 발생하면 정해진 예외가 발생한다")
    void uploadByByte_ShouldThrowS3Exception_WhenGeneralExceptionOccurs() {
        byte[] imageBytes = "fake image data".getBytes();

        S3UtilImpl s3UtilSpy = spy(s3Util);
        doThrow(new RuntimeException("Unexpected error"))
                .when(amazonS3).putObject(any(PutObjectRequest.class));

        S3Exception exception = assertThrows(S3Exception.class, () ->
                s3UtilSpy.uploadByByte("dir", imageBytes));
        assertEquals(ErrorCode.IMAGE_UPLOAD_IO_EXCEPTION, exception.getErrorCode());
    }


    @Test
    @DisplayName("delete()를 통해 이미지를 S3에서 삭제 할 수 있다")
    void delete_ShouldDeleteSuccessfully() {
        String filename = "test/image.png";

        doNothing().when(amazonS3).deleteObject(bucket, filename);
        when(amazonS3.doesObjectExist(bucket, filename)).thenReturn(false);

        assertDoesNotThrow(() -> s3Util.delete(filename));
    }


    @Test
    @DisplayName("delete()를 하고도 이미지가 남아있다면 정해진 예외를 반환한다")
    void delete_ShouldThrowException_WhenStillExists() {
        String key = "test/image.png";

        doNothing().when(amazonS3).deleteObject(bucket, key);
        when(amazonS3.doesObjectExist(bucket, key)).thenReturn(true);

        assertThrows(S3Exception.class, () -> s3Util.delete(key));
    }
}
