package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class RefreshTokenNotAvailable extends DefaultException {

    public RefreshTokenNotAvailable(String message) {
        super(message);
    }
}
