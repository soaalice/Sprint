package mg.annotation.validations.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mg.annotation.validations.validateur.RequiredValidator;

@Validation(RequiredValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Required {
    String message() default "Une exception 'not null' a été levée";
}
