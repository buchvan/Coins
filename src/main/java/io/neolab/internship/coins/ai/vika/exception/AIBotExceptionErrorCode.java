package io.neolab.internship.coins.ai.vika.exception;

import org.jetbrains.annotations.NotNull;

public enum AIBotExceptionErrorCode {

    DECISION_NOT_EXISTS("Decision doesnt exists!");

    private final @NotNull String message;

    AIBotExceptionErrorCode(final @NotNull String message) {
        this.message = message;
    }

    public @NotNull String getMessage() {
        return message;
    }
}
