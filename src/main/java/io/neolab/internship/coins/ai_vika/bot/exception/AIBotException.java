package io.neolab.internship.coins.ai_vika.bot.exception;

import org.jetbrains.annotations.NotNull;

public class AIBotException extends Exception {
    private final @NotNull AIBotExceptionErrorCode errorCode;

    public AIBotException(final @NotNull AIBotExceptionErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public @NotNull AIBotExceptionErrorCode getErrorCode() {
        return errorCode;
    }
}
