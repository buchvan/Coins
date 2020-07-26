package io.neolab.internship.coins.exceptions;

import org.jetbrains.annotations.NotNull;

public class CoinsException extends Exception {
    private final @NotNull CoinsErrorCode errorCode;

    public CoinsException(final @NotNull CoinsErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public @NotNull CoinsErrorCode getErrorCode() {
        return errorCode;
    }
}
