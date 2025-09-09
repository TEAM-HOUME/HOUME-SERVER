package or.sopt.houme.domain.credit.entity;

public enum CreditStatus {
    ACTIVE,  // 사용가능
    EXPIRED,  // 만료됨
    PENDING,    // 차감 대기 중 (이미지 생성 작업 진행 중)
    REVOKED  // 회수됨(회원탈퇴 등)
}
