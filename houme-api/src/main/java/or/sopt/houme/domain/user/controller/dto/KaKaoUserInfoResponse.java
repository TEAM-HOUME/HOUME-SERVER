package or.sopt.houme.domain.user.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
public class KaKaoUserInfoResponse {

    private Long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

    @Getter
    @Setter
    public static class Properties {
        private String nickname;
    }

    @Getter
    @Setter
    public static class KakaoAccount {
        private String email;
        private Boolean is_email_verified;
        private Boolean has_email;
        private Boolean profile_nickname_needs_agreement;
        private Boolean email_needs_agreement;
        private Boolean is_email_valid;
        private Profile profile;

        @Getter
        @Setter
        public static class Profile {
            private String nickname;
            private Boolean is_default_nickname;
        }
    }
}
