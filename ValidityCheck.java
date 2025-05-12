import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.System.exit;


/* This code is designed to validate the following types of numbers represented as strings:
        1. Personal identity number (Personnummer)
            - 10 or 12 digits, formatted as (YY)YYYYMMDD(+)-XXX check digit.
            - The first part represents the birthdate.
            - The last digit is a check digit calculated using Luhn's algorithm.
        2. Coordination number (Samordningsnummer)
            - Follows the same rules as the personal identity number.
            - The day value is increased by 60 (i.e., valid days are between 61-91).
        3. Organization number (Organisationsnummer)
            - If 12 digits long, the number must start with "16".
            - The middle two digits must be at least 20.
            - Follows the same validation rules as the prior numbers.
*/
class ValidityCheck{
    /* Exception for input with incorrect number format */
    static class InvalidNumberFormatException extends RuntimeException {
        public InvalidNumberFormatException(String message) {
            super(message);
        }
    }



    /*Validation of check digit (kontrollsiffra) based on Luhns Algoritm*/
    public static boolean validateCheckDigit(char[] number, int offset){
        int sum = 0;
        boolean alternate = true;
        //
        for (int i = offset; i < number.length - 1; i++) {
            // Ignore special symbols (either + or -)
            if (Character.isDigit(number[i])) {
                int num = number[i] - '0';
                // Every other number is doubled
                num = alternate ? num * 2 : num;
                // Adds the digit sum of the number to the sum
                sum += (num / 10) + (num % 10);
                alternate = !alternate;
            }
        }
        // Calculation of check digit based on given formula
        int checkDigitCalculated = (10 - (sum % 10)) % 10;
        // Compare calculated check digit with actual check digit
        return checkDigitCalculated == (number[number.length - 1] - '0');
    }

    /*Validation of date and date format*/
    public static boolean validateDate(String date) {
        DateTimeFormatter formatter;
        // Choose the correct format based on the length of the date
        if (date.length() == 8) {
            // 4-digit year format: uuuuMMdd
            formatter = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
        } else if (date.length() == 6) {
            // 2-digit year format: uuMMdd
            formatter = DateTimeFormatter.ofPattern("uuMMdd").withResolverStyle(ResolverStyle.STRICT);
        } else { //
            return false;
        }

        try {
            // Try to parse the date
            LocalDate parsedDate = LocalDate.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            // If an exception is thrown, the date is invalid.
            return false;
        }
    }

    /**
     * Validates the person number (personnummer) by checking the date and the check digit.
     */
    public static boolean validatePersonNumber(char[] number, int offset){
        System.out.println("\nValiderar personnummer: ");

        boolean dateIsValid = validateDate(new String(number, 0, 6+offset));
        if(!dateIsValid) {
            System.out.println("    - Ogiltigt datum");
            log("Ogiltigt datum för personnummer: " + new String(number));
            return false;
        }
        System.out.println("    - Giltigt datum");

        boolean checkDigitIsValid = validateCheckDigit(number, offset);
        if (!checkDigitIsValid) {
            System.out.println("    - Ogiltig kontrollsiffra");
            log("Ogiltig kontrollsiffra för personnummer: " + new String(number));
            return false;
        }
        System.out.println("    - Giltig kontrollsiffra");
        return true;
    }

    /*Validate coordination number (samordningsnummer) by checking the date (+60) and the check digit*/
    public static boolean validateCoordinationNumber(char[] number, int offset){
        System.out.println("\nValiderar samordningsnummer: ");

        char tmp = number[4 + offset];
        // Modify to date-60 for validation of date
        number[4 + offset] = (char) (number[4 + offset] - '6' + '0');
        boolean dateIsValid = validateDate(new String(number, 0, 6 + offset));
        // Reset original value
        number[4 + offset] = tmp;
        if (!dateIsValid) {
            System.out.println("    - Ogiltigt datum (-60)");
            log("Ogiltigt datum för samordningsnummer: " + new String(number));
            return false;
        }
        System.out.println("    - Giltigt datum");

        boolean checkDigitIsValid = validateCheckDigit(number, offset);
        if (!checkDigitIsValid) {
            System.out.println("    - Ogiltig kontrollsiffra");
            log("Ogiltig kontrollsiffra för samordningsnummer: " + new String(number));
            return false;
        }
        System.out.println("    - Giltig kontrollsiffra");

        return true;
    }
    /* Validates organization number (organisationsnummer) by checking format and check digit */
    public static boolean validateOrganisationNumber(char[] number, int offset){
        System.out.println("\nValiderar organisationsnummer: ");
        if (number.length >= 12 && (number[0] != '1' || number[1] != '6')) {
            System.out.println("    - Ogiltigt inledande sifferpar");
            log("Ogiltigt inledande sifferpar för organisationsnummer: " + new String(number));
            return false;
        }
        System.out.println("    - Giltigt inledande sifferpar");

        if (number[2 + offset] == '0' || number[2 + offset] == '1') {
            System.out.println("    - Mittersta sifferparet är mindre än 20");
            log("   - Mittersta sifferparet är mindre än 20 för organisationsnummer: " + new String(number));
            return false;
        }
        System.out.println("    - Mittersta sifferparet är minst 20");
        boolean checkDigitIsValid = validateCheckDigit(number, offset);
        if (!checkDigitIsValid) {
            System.out.println("    - Ogiltig kontrollsiffra för organisationsnummer: " + new String(number));
            log("   - Ogiltig kontrollsiffra");
            return false;
        }
        System.out.println("    - Giltig kontrollsiffra");

        return true;
    }
    /* Validates the number format*/
    public static void validateNumberFormat(char[] nummer){
        // The number must be between 10 and 13 characters long, including optional separator
        if(nummer.length > 13 || nummer.length < 10){
            throw new InvalidNumberFormatException("Ogiltigt format, " +
                    "nummret måste innehålla mellan 10 och 13 tecken");
        }
        int specialPos = -1;
        if(nummer.length == 13) //YYYYMMDD-XXXK
            specialPos = 8;
        else if(nummer.length == 11) //YYMMDD-XXXK
            specialPos = 6;
        for(int i = 0; i < nummer.length; i++){
            if(i == specialPos && (nummer[i] == '-' || nummer[i] == '+'))
                continue;
            if(!Character.isDigit(nummer[i]))
                throw new InvalidNumberFormatException(
                        "Ogiltigt format: Numret måste följa något av formaten " +
                                "YYMMDDXXXK, " +
                                "YYMMDD-XXXK, " +
                                "YYYYMMDDXXXK eller " +
                                "YYYYMMDD-XXXK"
                );
        }

    }
    /* Setup for logger */
    private static final Logger logger = Logger.getLogger(ValidityCheck.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("validation.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevents print to console
        } catch (IOException e) {
            System.out.println("Kunde inte skapa loggfilen.");
        }
    }
    /* Logs messages to the log file */
    public static void log(String message) {
        logger.info(message);
    }

    /**
     * Main method for ValidityChecks.
     *
     * Expected input: A single number string as an argument.
     *
     * The program performs:
     * 1. Format validation.
     * 2. Identification of number type (Personnummer, Samordningsnummer, Organisationsnummer).
     * 3. Logging of failures and printing of results.
     */
    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Argument saknas!\n" +
                    "Användning: java ValidityCheck.java <nummer> ");
            exit(0);
        }
        String number = args[0];
        char[] numberArr = number.toCharArray();
        // If the number has 12 or more digits, set offset to 2, otherwise 0
        int offset = numberArr.length >= 12 ? 2 : 0;

        try {
            System.out.println("------------------------------------------");
            System.out.println("Påbörjar validation av nummer: " + number);

            // Validates the format of given number
            System.out.print("    - Nummret är av giltigt format: ");
            validateNumberFormat(numberArr);
            System.out.println(true);

            // Validates the type of number
            if (validatePersonNumber(numberArr, offset)) {
                System.out.println("Nummer " + number + " är ett giltigt personnummer");
            } else if (validateCoordinationNumber(numberArr, offset)) {
                System.out.println("Nummer " + number + " är ett giltigt samordningsnummer");
            } else if (validateOrganisationNumber(numberArr, offset)) {
                System.out.println("Nummer " + number + " är ett giltigt organisationsnummer");
            } else {
                System.out.println("\nNummer " + number + " är ogiltigt");
            }
        } catch (InvalidNumberFormatException e) {
            System.out.println(false);
            System.out.println("Fel: " + e.getMessage());
        }
    }
}