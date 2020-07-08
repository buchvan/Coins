package io.neolab.internship.coins.exceptions;

public class CoinsException {
    private ErrorCode errorCode;
    private String param;

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
