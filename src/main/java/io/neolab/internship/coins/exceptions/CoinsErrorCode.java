package io.neolab.internship.coins.exceptions;

import org.jetbrains.annotations.NotNull;

public enum CoinsErrorCode {
    CLIENT_CREATION_FAILED("Client creation failed"),
    CLIENT_DISCONNECTION("Client disconnection"),
    WRONG_BOARD_SIZES("Wrong board sizes"),
    ANSWER_VALIDATION_ERROR_EMPTY_ANSWER("Answer is empty"),
    ANSWER_VALIDATION_UNAVAILABLE_NEW_RACE("New race in available in races pool"),
    SAME_RACES("Current and new races are the same"),
    ANSWER_VALIDATION_WRONG_POSITION("Position is wrong"),
    ANSWER_VALIDATION_NO_AVAILABLE_UNITS("No available units for cell attempt"),
    ANSWER_VALIDATION_CELL_CAPTURE_IMPOSSIBLE("Cell capture impossible"),
    ANSWER_VALIDATION_NO_PLACE_FOR_DISTRIBUTION("No place for distribution"),
    ANSWER_VALIDATION_NOT_ENOUGH_UNITS("No enough units for distribution"),
    PLAYERS_LIST_IS_NULL("List of players is null"),
    QUESTION_TYPE_NOT_FOUND("Type of question not found"),
    MESSAGE_TYPE_NOT_FOUND("Type of message not found"),
    ANSWER_VALIDATION_ENTER_CELL_IMPOSSIBLE("Impossible enter to cell"),
    ANSWER_VALIDATION_ENTER_CELL_INVALID_UNITS("Invalid units in entering to cell"),
    ANSWER_VALIDATION_UNREACHABLE_CELL("Cell is unreachable for capture"),
    GAME_OVER("Game over"),
    CLIENT_CONFIG_LOADING_FAILED("Client configuration loading failed"),
    SERVER_CONFIG_LOADING_FAILED("Server configuration loading failed"),
    ACTION_TYPE_NOT_FOUND("Action type not found"),
    PLAYER_NOT_FOUND("Player not found"),
    LOGIC_ERROR("Logic error"),
    ;

    private final @NotNull String message;

    CoinsErrorCode(final @NotNull String message) {
        this.message = message;
    }

    public @NotNull String getMessage() {
        return message;
    }
}
