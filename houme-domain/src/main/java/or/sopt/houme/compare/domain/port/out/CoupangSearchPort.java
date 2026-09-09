package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.CoupangCandidate;

import java.util.List;

public interface CoupangSearchPort {

    List<CoupangCandidate> findCandidatesByKeyword(String keyword);
}
