package or.sopt.houme.global.util;

public interface BCrypt {

    String hash(String password);
    boolean isMatch(String password, String hashedPassword);
}
