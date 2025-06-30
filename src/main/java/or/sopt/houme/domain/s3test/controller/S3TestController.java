package or.sopt.houme.domain.s3test.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.s3test.service.S3TestService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/s3-test")
@RequiredArgsConstructor
public class S3TestController {

    private final S3TestService service;


    @Operation(summary = "S3 이미지 업로드 테스트 API")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String save(@RequestParam("file") MultipartFile file) {

        return service.uploadImage(file);
    }


    @Operation(summary = "S3 이미지 삭제 테스트 API")
    @PostMapping("/delete")
    public void delete(@RequestParam("filename") String filename) {

        service.deleteImage(filename);
    }
}
