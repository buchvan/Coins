package io.neolab.internship.coins.exceptions;

import org.jetbrains.annotations.NotNull;

public enum ErrorCode {
    CLIENT_CREATION_FAILED("Client creation failed"),
    WRONG_BOARD_SIZES("Wrong board sizes"),
    EMPTY_ANSWER("Answer is empty"),
    UNAVAILABLE_NEW_RACE("New race in available in races pool"),
    WRONG_POSITION("Position is wrong"),
    INVALID_ACHIEVABLE_CELL("Invalid achievable cells to cell attempt"),
    NO_AVAILABLE_UNITS("No available units for cell attempt"),
    CELL_CAPTURE_IMPOSSIBLE("Cell capture impossible"),
    NO_PLACE_FOR_DISTRIBUTION("No place for distribution"),
    NOT_ENOUGH_UNITS("No enough units for distribution"),
    PLAYERS_LIST_IS_NULL("List of players is null"),
    QUESTION_TYPE_NOT_FOUND("Type of question not found"),
    ;

    private final @NotNull String message;

    ErrorCode(final @NotNull String message) {
        this.message = message;
    }

    public @NotNull String getMessage() {
        return message;
    }
}
