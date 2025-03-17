package ssafy.d210.backend.exception;

public class DefaultException extends RuntimeException{

    private static final String MESSAGE = "Unknown Exception";

    public DefaultException() {
        super(MESSAGE);
    }

    public DefaultException(String message) {
        super(message);
    }

    public DefaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefaultException(Throwable cause) {
        super(cause);
    }

}
