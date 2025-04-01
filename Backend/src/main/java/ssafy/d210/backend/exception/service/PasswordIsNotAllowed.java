package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class PasswordIsNotAllowed extends DefaultException {

    public PasswordIsNotAllowed (String message) {
        super(message);
    }
}
