package mg.annotation.validations.validateur;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import mg.annotation.validations.annotation.DateFormat;

public class DateValidator extends Validator<DateFormat>{
    public DateValidator(){
        super(DateFormat.class);
    }

    @Override
    public Exception validate(Object o, DateFormat annotation) {
        // Récupérer le format spécifié dans l'annotation
        String format = annotation.format();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        if (o instanceof String dateStr) {
            try {
                // Tenter de parser la chaîne de caractères
                LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Retourner une exception personnalisée si le format est incorrect
                return new IllegalArgumentException("La date '" + dateStr + "' est invalide ou n'est pas au format attendu : " + format);
            }
        }
        else if (o instanceof LocalDate date) {
            // Tenter de formater la date LocalDate et comparer avec l'entrée d'origine
            String formattedDate = date.format(formatter);
            // Si la date formatée ne correspond pas à l'originale, c'est une erreur
            if (!formattedDate.equals(o.toString())) {
                return new IllegalArgumentException("La date '" + o + "' n'est pas au format attendu : " + format);
            }
        } else {
            return new IllegalArgumentException("Votre attribut/paramètre n'est ni une chaîne de caractères ni un LocalDate.");
        }

        return null;
    }

}
