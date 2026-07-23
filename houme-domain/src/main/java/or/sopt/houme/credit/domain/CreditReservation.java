package or.sopt.houme.credit.domain;

/**
 * 예약된(PENDING) 크레딧 1건을 가리키는 도메인 핸들(값 객체).
 *
 * <p>이미지 생성 흐름은 JPA 엔티티 대신 이 핸들만 주고받는다("번호표").
 * 예약 후 성공하면 {@code commit}, 실패하면 {@code rollback} 에 이 핸들을 넘긴다.
 */
public record CreditReservation(Long creditId, Long userId) {
}
