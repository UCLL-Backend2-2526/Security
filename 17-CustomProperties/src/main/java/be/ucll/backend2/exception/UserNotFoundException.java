package be.ucll.backend2.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(long id) {
        super("Could not find user with id " + id);
    }
}
