package io.neolab.internship.coins.exceptions;

import org.jetbrains.annotations.NotNull;

public enum UtilsErrorCode {
    INDEX_OUT_OF_BOUNDS("Index out of bounds"),
    ;

    private final @NotNull String message;

    UtilsErrorCode(final @NotNull String message) {
        this.message = message;
    }

    public @NotNull String getMessage() {
        return message;
    }
}
