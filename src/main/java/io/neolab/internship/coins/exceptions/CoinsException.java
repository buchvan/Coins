package io.neolab.internship.coins.exceptions;

import java.util.Objects;

public class CoinsException {
    private ErrorCode errorCode;
    private String param;

    public CoinsException(ErrorCode errorCode, String param) {
        this.errorCode = errorCode;
        this.param = param;
    }

    public CoinsException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
