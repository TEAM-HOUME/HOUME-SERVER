package or.sopt.houme.compare.infrastructure.ebay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EbayTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("token_type") String tokenType
) {}
