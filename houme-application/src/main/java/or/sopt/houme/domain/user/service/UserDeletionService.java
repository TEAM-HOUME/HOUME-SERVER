package or.sopt.houme.domain.user.service;

/** 회원 탈퇴(연관 데이터 일괄 삭제) 인바운드 계약 (#582 — 구현은 다도메인 리포를 다루므로 infra 측에 배정). */
public interface UserDeletionService {

    void delete(Long userId);
}
