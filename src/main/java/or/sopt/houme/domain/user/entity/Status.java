package or.sopt.houme.domain.user.entity;

public enum Status {
    ACTIVE,  // 로그인 가능한 활동 유저
    BANNED,  // 관리자에 의해 제재됨
    DELETED  // 탈퇴한 유저
}
