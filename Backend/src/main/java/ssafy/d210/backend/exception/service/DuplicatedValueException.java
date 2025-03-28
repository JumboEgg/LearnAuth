package ssafy.d210.backend.exception.service;
//
import ssafy.d210.backend.exception.DefaultException;

public class DuplicatedValueException extends DefaultException {
    public DuplicatedValueException(String message) {
        super(message);
    }
}
