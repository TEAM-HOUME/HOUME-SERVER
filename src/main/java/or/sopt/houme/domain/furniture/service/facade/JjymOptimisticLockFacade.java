package or.sopt.houme.domain.furniture.service.facade;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.service.JjymServiceImpl;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import static or.sopt.houme.global.util.constant.OptimisticLockConstant.MAX_RETRIES;
import static or.sopt.houme.global.util.constant.OptimisticLockConstant.RETRY_DELAY_MS;

@Component
@RequiredArgsConstructor
public class JjymOptimisticLockFacade {

    private final JjymServiceImpl jjymService;

    public boolean toggle(User user, Long recommendFurnitureId) {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                return jjymService.jjymToggle(user.getId(), recommendFurnitureId);
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                long backoffTime = (long) Math.pow(2, retryCount) * RETRY_DELAY_MS;
                try {
                    Thread.sleep(backoffTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DataIntegrityViolationException("찜 토글 처리 중 인터럽트가 발생했습니다", ie);
                }
                retryCount++;
            }
        }

        // 마지막까지 실패 시 런타임 예외 전파 (글로벌 핸들러에서 처리)
        throw new DataIntegrityViolationException("찜 시도가 정해진 횟수를 초과하였습니다");
    }
}
