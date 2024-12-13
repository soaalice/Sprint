package mg.annotation.validations.validateur;

import java.lang.annotation.Annotation;

public abstract class Validator<T extends Annotation> {
    private final Class<T> annotation;

    public Validator(Class<T> annotation){
        this.annotation=annotation;
    }

    public Class<T> getAnnotation(){
        return this.annotation;
    }

    public Exception validateInside(Object argumentObjet,Annotation annotationField){
        return this.validate(argumentObjet, this.getAnnotation().cast(annotationField));
    }

    public abstract Exception validate(Object o,T annotation);
}
