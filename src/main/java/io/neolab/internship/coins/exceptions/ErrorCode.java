package io.neolab.internship.coins.exceptions;

public enum ErrorCode {
    CLIENT_CREATION_FAILED("Client creation failed"),
    WRONG_BOARD_SIZES("Wrong board sizes"),
    EMPTY_ANSWER("Answer is empty"),
    UNAVAILABLE_NEW_RACE("New race in available in races pool"),
    SAME_RACES("Current and new races are the same"),
    WRONG_POSITION("Position is wrong"),
    NO_ACHIEVABLE_CELL("No achievable cells to cell attempt"),
    NO_AVAILABLE_UNITS("No available units for cell attempt"),
    CELL_CAPTURE_IMPOSSIBLE("Cell capture impossible"),
    NO_PLACE_FOR_DISTRIBUTION("No place for distribution"),
    NOT_ENOUGH_UNITS("No enough units for distribution"),
    PLAYERS_LIST_IS_NULL("List of players is null"),
    QUESTION_TYPE_NOT_FOUND("Type of question not found"),
    ;

    private final String message;

    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
