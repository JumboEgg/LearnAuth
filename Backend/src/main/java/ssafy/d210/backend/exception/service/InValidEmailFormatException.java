package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class InValidEmailFormatException extends DefaultException {
    public InValidEmailFormatException(String message) {
        super(message);
    }
}
