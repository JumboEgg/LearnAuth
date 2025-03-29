package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class DecryptedException extends DefaultException {

    public DecryptedException(String message, Throwable e) {
        super(message, e);
    }
}
