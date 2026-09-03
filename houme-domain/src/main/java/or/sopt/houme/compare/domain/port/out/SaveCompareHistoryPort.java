package or.sopt.houme.compare.domain.port.out;

public interface SaveCompareHistoryPort {

    void save(Long userId, String sourceUrl, String title, String thumbnail, Long price);
}
