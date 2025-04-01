package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class InvalidSearchKeywordException extends DefaultException {
    public InvalidSearchKeywordException(String message) {
        super (message);
    }
}
