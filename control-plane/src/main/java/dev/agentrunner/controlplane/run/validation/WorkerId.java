package dev.agentrunner.controlplane.run.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Pattern(regexp = "^[A-Za-z0-9-]+$", message = "must contain only letters, digits, and hyphens")
@Size(max = 64, message = "must be at most 64 characters")
@Constraint(validatedBy = {})
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WorkerId {

    String message() default "invalid worker id";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
