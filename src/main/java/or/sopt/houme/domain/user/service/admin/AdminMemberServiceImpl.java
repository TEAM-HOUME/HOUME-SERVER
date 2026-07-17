package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.credit.application.CreditUseCase;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminCreditGrantResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminMemberResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.member.response.AdminMemberSearchResponse;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberServiceImpl implements AdminMemberService {

    private static final int MEMBER_SEARCH_LIMIT = 20;

    private final UserRepository userRepository;
    private final CreditUseCase creditUseCase;

    @Override
    public AdminMemberSearchResponse searchMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new AdminMemberSearchResponse(List.of());
        }

        List<AdminMemberResponse> members = userRepository.searchMembers(keyword.trim(), MEMBER_SEARCH_LIMIT)
                .stream()
                .map(user -> new AdminMemberResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getNicknameTag(),
                        user.getEmail(),
                        creditUseCase.countActive(user.getId())
                ))
                .toList();

        return new AdminMemberSearchResponse(members);
    }

    @Override
    @Transactional
    public AdminCreditGrantResponse grantCredits(Long memberId, int amount) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        long balance = creditUseCase.grant(member.getId(), amount);

        return new AdminCreditGrantResponse(member.getId(), amount, balance);
    }
}
