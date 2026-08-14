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
                @Index(name = "idx_legacy_api_call_history_endpoint_created_at", columnList = "method, api_path, created_at")
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

    @Column(name = "api_path", nullable = false, length = 512)
    private String apiPath;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "trace_id", nullable = false, length = 36)
    private String traceId;

    @Builder
    private LegacyApiCallHistoryJpaEntity(
            String method,
            String requestUri,
            String apiPath,
            Long userId,
            String traceId
    ) {
        this.method = method;
        this.requestUri = requestUri;
        this.apiPath = apiPath;
        this.userId = userId;
        this.traceId = traceId;
    }
}
