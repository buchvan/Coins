package io.neolab.internship.coins.exceptions;

public class CoinsException extends Exception {
    private final ErrorCode errorCode;

    public CoinsException(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
