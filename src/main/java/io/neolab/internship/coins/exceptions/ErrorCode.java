package io.neolab.internship.coins.exceptions;

public enum ErrorCode {
    WRONG_BOARD_SIZES("Wrong board sizes");
    //other...

    private final String message;


    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
