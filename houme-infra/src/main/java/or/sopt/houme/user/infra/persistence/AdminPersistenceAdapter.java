package or.sopt.houme.user.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.repository.admin.AdminRepository;
import or.sopt.houme.user.domain.port.out.AdminRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * {@link AdminRepositoryPort} 의 JPA 구현 어댑터. 기존 {@link AdminRepository} 를 재사용한다.
 */
@Component
@RequiredArgsConstructor
public class AdminPersistenceAdapter implements AdminRepositoryPort {

    private final AdminRepository adminRepository;

    @Override
    public boolean existsById(Long adminId) {
        return adminId != null && adminRepository.existsById(adminId);
    }
}
