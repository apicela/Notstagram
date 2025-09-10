package apicela.notstagram.exceptions;

public class UsernameAlreadyInUseException extends RuntimeException {
    public UsernameAlreadyInUseException(String message) {
        super(message);
    }

    public UsernameAlreadyInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}