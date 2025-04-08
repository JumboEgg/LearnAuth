package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class TokenNotInDB extends DefaultException {

    public TokenNotInDB(String message) {
        super(message);
    }
}
