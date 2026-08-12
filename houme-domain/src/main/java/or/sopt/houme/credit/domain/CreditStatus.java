package or.sopt.houme.credit.domain;

/**
 * 크레딧 상태. 순수 도메인 enum (JPA/프레임워크 비의존).
 */
public enum CreditStatus {
    ACTIVE,   // 사용 가능
    EXPIRED,  // 만료됨
    PENDING,  // 차감 대기 중 (이미지 생성 작업 진행 중)
    REVOKED   // 회수됨 (회원탈퇴 등)
}
