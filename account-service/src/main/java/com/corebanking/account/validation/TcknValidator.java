package com.corebanking.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TcknValidator implements ConstraintValidator<ValidTckn, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() != 11 || !value.matches("\\d+")) {
            return false;
        }

        if (value.startsWith("0")) {
            return false;
        }

        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(value.charAt(i));
        }

        if (digits[10] % 2 != 0) {
            return false;
        }

        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];

        int tenthDigit = ((oddSum * 7) + (evenSum * 9)) % 10;
        if (digits[9] != tenthDigit) {
            return false;
        }

        int firstTenSum = 0;
        for (int i = 0; i < 10; i++) {
            firstTenSum += digits[i];
        }

        int eleventhDigit = firstTenSum % 10;
        if (digits[10] != eleventhDigit) {
            return false;
        }

        return true;
    }
}
