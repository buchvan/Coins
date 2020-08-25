package io.neolab.internship.coins.ai_vika.bot.exception;

import org.jetbrains.annotations.NotNull;

public enum AIBotExceptionErrorCode {

    DECISION_NOT_EXISTS("Decision doesnt exists!"),
    NO_FOUND_PLAYER("Player was not found");

    private final @NotNull String message;

    AIBotExceptionErrorCode(final @NotNull String message) {
        this.message = message;
    }

    public @NotNull String getMessage() {
        return message;
    }
}
