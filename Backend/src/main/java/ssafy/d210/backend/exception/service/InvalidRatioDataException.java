package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class InvalidRatioDataException extends DefaultException {
    public InvalidRatioDataException(String message) {
        super (message);
    }
}
