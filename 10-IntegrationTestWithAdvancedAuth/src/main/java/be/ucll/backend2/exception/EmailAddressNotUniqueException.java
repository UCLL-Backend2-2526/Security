package be.ucll.backend2.exception;

public class EmailAddressNotUniqueException extends Exception {
    public EmailAddressNotUniqueException(String emailAddress) {
        super("A user with the e-mail address " + emailAddress + " is already registered.");
    }
}
