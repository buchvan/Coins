package io.neolab.internship.coins.exceptions;

public enum ErrorCode {
    WRONG_BOARD_SIZES("Wrong board sizes"),
    EMPTY_ANSWER("Answer is empty"),
    UNAVAILABLE_NEW_RACE("New race in available in races pool"),
    SAME_RACES("Current and new races are the same"),
    WRONG_POSITION("Position is wrong"),
    NO_ACHIEVABLE_CELLS("No achievable cells to cell attempt"),
    NO_AVAILABLE_UNITS("No available units for cell attempt"),
    CELL_CAPTURE_IMPOSSIBLE("Cell capture impossible"),
    NO_PLACE_FOR_DISTRIBUTION("No place for distribution"),
    NOT_ENOUGH_UNITS("No enough units for distribution");
    //other...

    private final String message;


    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
