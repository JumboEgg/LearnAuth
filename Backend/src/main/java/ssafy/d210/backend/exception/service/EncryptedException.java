package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class EncryptedException extends DefaultException {

    public EncryptedException(String message, Throwable e) {
        super(message, e);
    }
}
