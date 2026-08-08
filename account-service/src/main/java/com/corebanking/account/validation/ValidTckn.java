package com.corebanking.account.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.corebanking.account.common.ErrorCodes;

@Documented
@Constraint(validatedBy = TcknValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTckn {
    String message() default ErrorCodes.VAL_INVALID_TCKN;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
