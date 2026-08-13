package or.sopt.houme.legacyapi.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "legacy_api_call_histories",
        indexes = {
                @Index(name = "idx_legacy_api_call_history_created_at", columnList = "created_at"),
                @Index(name = "idx_legacy_api_call_history_endpoint_created_at", columnList = "method, request_uri, created_at")
        }
)
public class LegacyApiCallHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(name = "request_uri", nullable = false, length = 512)
    private String requestUri;

    @Column(name = "user_id")
    private Long userId;

    @Builder
    private LegacyApiCallHistoryJpaEntity(
            String method,
            String requestUri,
            Long userId
    ) {
        this.method = method;
        this.requestUri = requestUri;
        this.userId = userId;
    }
}
