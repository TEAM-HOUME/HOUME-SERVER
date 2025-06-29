package or.sopt.houme.domain.user.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KaKaoUserInfoResponse {

    private Long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

    @Getter
    public static class Properties {
        private String nickname;
    }

    @Getter
    public static class KakaoAccount {
        private String email;
        private Boolean is_email_verified;
        private Boolean has_email;
        private Boolean profile_nickname_needs_agreement;
        private Boolean email_needs_agreement;
        private Boolean is_email_valid;
        private Profile profile;

        @Getter
        public static class Profile {
            private String nickname;
            private Boolean is_default_nickname;
        }
    }
}
