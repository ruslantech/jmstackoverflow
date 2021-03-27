package com.javamentor.qa.platform.models.entity.user.reputation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = ReputationValidator.class)
public @interface CombinedNotNull {
    String message() default "questionId or answerId is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields() default {};
}