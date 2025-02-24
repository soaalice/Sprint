package dev.exceptions;

public class FieldValidationException extends Exception {
    private String fieldName;
    private String message;

    public FieldValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getMessage() {
        return "Erreur de validation pour le champ: " + fieldName + " - " + message;
    }

}
