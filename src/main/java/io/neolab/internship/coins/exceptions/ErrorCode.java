package io.neolab.internship.coins.exceptions;

public enum ErrorCode {
    CLIENT_CREATION_FAILED("Client creation failed"),
    WRONG_BOARD_SIZES("Wrong board sizes"),
    ANSWER_VALIDATION_ERROR_EMPTY_ANSWER("Answer is empty"),
    ANSWER_VALIDATION_UNAVAILABLE_NEW_RACE("New race in available in races pool"),
    SAME_RACES("Current and new races are the same"),
    ANSWER_VALIDATION_WRONG_POSITION("Position is wrong"),
    NO_ACHIEVABLE_CELL("No achievable cells to cell attempt"),
    ANSWER_VALIDATION_NO_AVAILABLE_UNITS("No available units for cell attempt"),
    ANSWER_VALIDATION_CELL_CAPTURE_IMPOSSIBLE("Cell capture impossible"),
    ANSWER_VALIDATION_NO_PLACE_FOR_DISTRIBUTION("No place for distribution"),
    ANSWER_VALIDATION_NOT_ENOUGH_UNITS("No enough units for distribution"),
    PLAYERS_LIST_IS_NULL("List of players is null"),
    QUESTION_TYPE_NOT_FOUND("Type of question not found"),
    ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE("Impossible enter to cell"),
    ANSWER_VALIDATION_UNREACHABLE_CELL("Cell is unreachable for capture")
    ;

    private final String message;

    ErrorCode(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
