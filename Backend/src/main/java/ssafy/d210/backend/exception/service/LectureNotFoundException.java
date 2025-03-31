package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class LectureNotFoundException extends DefaultException {
    public LectureNotFoundException(String message) {
        super(message);
    }
}
