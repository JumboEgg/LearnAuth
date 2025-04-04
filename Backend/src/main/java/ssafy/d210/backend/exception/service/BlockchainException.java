package ssafy.d210.backend.exception.service;

import ssafy.d210.backend.exception.DefaultException;

public class BlockchainException extends DefaultException {
    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
