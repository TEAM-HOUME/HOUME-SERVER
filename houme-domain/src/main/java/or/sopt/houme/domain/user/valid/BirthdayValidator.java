package or.sopt.houme.domain.user.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class BirthdayValidator implements ConstraintValidator<ValidBirthday, String> {

    @Override
    public boolean isValid(String birthdayStr, ConstraintValidatorContext context) {
        if (birthdayStr == null || birthdayStr.isBlank()) return false;

        LocalDate birthday;
        try {
            birthday = LocalDate.parse(birthdayStr);
        } catch (DateTimeParseException e) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (birthday.getYear() < 1900 || birthday.isAfter(today)) return false;

        return birthday.isBefore(today.minusYears(14)) || birthday.isEqual(today.minusYears(14));
    }
}
