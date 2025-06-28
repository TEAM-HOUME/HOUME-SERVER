package or.sopt.houme.global.util;

import org.springframework.stereotype.Component;

@Component
public class S3UtilImpl implements S3Util {

    @Override
    public void upload(byte[] image, String dirname){

        throw new IllegalArgumentException("S3를 활용한 upload 메서드가 아직 구현되지 않았습니다");
    }
}
