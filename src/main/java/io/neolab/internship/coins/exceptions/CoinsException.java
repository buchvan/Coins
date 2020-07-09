package io.neolab.internship.coins.exceptions;

public class CoinsException extends Exception {
    private ErrorCode errorCode;

    public CoinsException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

}
