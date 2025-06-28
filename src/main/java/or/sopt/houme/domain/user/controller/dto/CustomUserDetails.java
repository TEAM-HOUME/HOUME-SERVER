package or.sopt.houme.domain.user.controller.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    /**
     * 여기에 회원 정보를 저장해서 우리가 회원정보를 가져올 수 있게 됩니다!
     *
     * 해당 세션의 생명주기는 하나의 요청에 한정됩니다
     * 새로운 요청이 들어오면 UserDetail 은 초기화 됩니다
     * */
    private final User user;

    /**
     * 사용자의 권한 정보를 반환합니다.
     *
     * @return 사용자의 역할(Role)에 해당하는 권한 컬렉션을 반환합니다. 사용자가 없을 경우 권한 값은 null이 될 수 있습니다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> {
            if (user != null) {
                return user.getRole().name();
            }
            return null;
        });
        return collection;
    }


    /**
     * 사용자의 비밀번호를 반환합니다.
     *
     * @return 사용자 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 소셜 로그인 방식으로 인해 사용자 이름(username)을 반환하지 않습니다.
     *
     * @return 항상 빈 문자열을 반환합니다.
     */
    @Override
    public String getUsername() {
        return "";
    }

    /**
     * 사용자의 이메일 주소를 반환합니다.
     *
     * @return 사용자의 이메일 주소
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 계정이 만료되지 않았음을 나타냅니다.
     *
     * @return 항상 true를 반환하여 계정이 만료되지 않았음을 의미합니다.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정이 잠겨 있지 않음을 나타냅니다.
     *
     * 항상 true를 반환하여 계정이 잠금 상태가 아님을 보장합니다.
     *
     * @return 계정이 잠겨 있지 않으면 true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 사용자의 인증 정보가 만료되지 않았음을 나타냅니다.
     *
     * @return 항상 true를 반환하여 인증 정보가 만료되지 않았음을 의미합니다.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 사용자가 항상 활성화되어 있음을 나타냅니다.
     *
     * @return 계정이 활성화된 경우 true를 반환합니다.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
