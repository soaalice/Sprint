package dev.exceptions;

import java.util.List;

public class ValidationException extends Exception {
    private List<Exception> exceptions;

    public ValidationException(List<Exception> exceptions){
        super();
        this.exceptions=exceptions;
    }

    @Override
    public String getMessage(){
        String message="";
        for (Exception exception : exceptions) {
            message+=exception.getMessage();
        }
        return message;
    }
}
