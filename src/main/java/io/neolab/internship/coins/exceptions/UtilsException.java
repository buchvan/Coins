package io.neolab.internship.coins.exceptions;

import org.jetbrains.annotations.NotNull;

public class UtilsException extends Exception {
    private final @NotNull UtilsErrorCode errorCode;

    public UtilsException(final @NotNull UtilsErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public @NotNull UtilsErrorCode getErrorCode() {
        return errorCode;
    }
}
