package ssafy.d210.backend.exception.service;

import org.bouncycastle.jcajce.provider.drbg.DRBG;
import ssafy.d210.backend.exception.DefaultException;

public class InvalidQuizDataException extends DefaultException {
    public InvalidQuizDataException(String message) {
        super(message);
    }
}
