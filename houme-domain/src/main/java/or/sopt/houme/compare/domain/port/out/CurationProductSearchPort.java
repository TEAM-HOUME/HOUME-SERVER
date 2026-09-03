package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.CurationCandidate;

import java.util.List;

public interface CurationProductSearchPort {

    List<CurationCandidate> findCandidates(String category, int limit);
}
