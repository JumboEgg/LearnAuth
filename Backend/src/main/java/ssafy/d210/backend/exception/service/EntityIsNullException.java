package ssafy.d210.backend.exception.service;
//
import ssafy.d210.backend.exception.DefaultException;

public class EntityIsNullException extends DefaultException {
    public EntityIsNullException(String message) {
        super(message);
    }
}
