package or.sopt.houme.global.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@MappedSuperclass
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 상태 값을 바꾸는 soft delete 로 사용
    @Column
    protected LocalDateTime deletedAt;

    /**
     * 엔티티를 소프트 삭제 처리하여 삭제 시각을 현재 시각으로 설정합니다.
     *
     * 이 메서드는 엔티티를 실제로 삭제하지 않고, `deletedAt` 필드에 현재 시각을 기록하여 논리적으로 삭제된 상태로 만듭니다.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    @CreatedBy
    @Column(updatable = false)
    protected String createdBy;

    @LastModifiedBy
    protected String updatedBy;
}
