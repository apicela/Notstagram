package apicela.notstagram.exceptions;


public class SaveException extends RuntimeException {
    public SaveException(String message) {
        super(message);
    }

    public SaveException(String message, Throwable cause) {
        super(message, cause);
    }
}