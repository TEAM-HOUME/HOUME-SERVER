package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.EbayCandidate;

import java.util.List;

public interface EbaySearchPort {
    List<EbayCandidate> search(String keyword, int limit);
    List<EbayCandidate> searchByImage(String base64, int limit);
}
