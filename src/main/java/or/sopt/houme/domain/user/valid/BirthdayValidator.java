package or.sopt.houme.domain.user.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class BirthdayValidator implements ConstraintValidator<ValidBirthday, LocalDate> {

    @Override
    public boolean isValid(LocalDate birthday, ConstraintValidatorContext context) {
        // 생년월일 비어있으면 false
        if (birthday == null) return false;

        LocalDate today = LocalDate.now();

        // 1900년보다 이전이거나 오늘 이후면 false
        if (birthday.getYear() < 1900 || birthday.isAfter(today)) {
            return false;
        }

        // 만 14세 이하는 false
        return birthday.isBefore(today.minusYears(14)) || birthday.isEqual(today.minusYears(14));
    }
}
