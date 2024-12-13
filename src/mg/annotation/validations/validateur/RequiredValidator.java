package mg.annotation.validations.validateur;

import mg.annotation.validations.annotation.Required;

public class RequiredValidator extends Validator<Required>{
    
    public RequiredValidator(){
        super(Required.class);
    }

    @Override
    public Exception validate(Object o, Required annotation){
        if(o==null){
            return new Exception(annotation.message());
        }
        return null;
    }
}
