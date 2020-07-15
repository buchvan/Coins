package io.neolab.internship.coins.exceptions;

public enum ErrorCode {
    WRONG_BOARD_SIZES("Wrong board sizes"),
    EMPTY_ANSWER("Empty answer"),
    PLAYERS_LIST_IS_NULL("List of players is null"),
    ;

    private final String message;


    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
