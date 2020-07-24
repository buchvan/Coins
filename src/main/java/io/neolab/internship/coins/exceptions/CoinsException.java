package io.neolab.internship.coins.exceptions;

public class CoinsException extends Exception {
    private ErrorCode errorCode;

    public CoinsException(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

}
