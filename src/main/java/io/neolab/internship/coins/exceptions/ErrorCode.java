package io.neolab.internship.coins.exceptions;

public enum ErrorCode {
    WRONG_BOARD_SIZES("Wrong board sizes"),
    EMPTY_ANSWER("Answer is empty"),
    UNAVAILABLE_NEW_RACE("New race in available in races pool"),
    SAME_RACES("Current and new races are the same");
    //other...

    private final String message;


    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
