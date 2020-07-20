package io.neolab.internship.coins.exceptions;

import org.jetbrains.annotations.NotNull;

public class CoinsException extends Exception {
    private final @NotNull ErrorCode errorCode;

    public CoinsException(final @NotNull ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public @NotNull ErrorCode getErrorCode() {
        return errorCode;
    }
}
