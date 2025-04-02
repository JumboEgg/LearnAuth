package ssafy.d210.backend.exception.service;

import org.bouncycastle.jcajce.provider.drbg.DRBG;
import ssafy.d210.backend.exception.DefaultException;

public class InvalidLectureDataException extends DefaultException {
    public InvalidLectureDataException(String message) {
        super(message);
    }
}
