package or.sopt.houme.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptImpl implements BCrypt {

    private final BCryptPasswordEncoder encoder;

    /**
     * 객체지향적 요소를 살리기 위해 BCryptPasswordEncoder 를 추상화하였습니다.
     * */
    @Override
    public String hash(String password) {
        return encoder.encode(password);
    }

    @Override
    public boolean isMatch(String password, String hashedPassword) {
        return encoder.matches(password, hashedPassword);
    }
}
