package mg.annotation.validations.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mg.annotation.validations.validateur.DateValidator;

@Validation(DateValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface DateFormat {
    String format() default "yyyy-MM-dd";
}
