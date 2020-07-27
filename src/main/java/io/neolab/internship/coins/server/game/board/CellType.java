package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public enum CellType implements Serializable {
    LAND("LAND", 1, 2, "L"),
    MUSHROOM("MUSHROOM", 1, 2, "m"),
    MOUNTAIN("MOUNTAIN", 1, 3, "M"),
    WATER("WATER", 1, 1, "W");

    @JsonProperty
    private final @NotNull String title;

    @JsonProperty
    private final @NotNull String view;

    @JsonProperty
    private final int catchDifficulty; // Сложность захвата клетки

    @JsonProperty
    private final int coinYield; // Число монет, которое приносит клетка

    @Contract(pure = true)
    @JsonCreator
    CellType(@NotNull @JsonProperty("title") final String title,
             @JsonProperty("coinYield") final int coinYield,
             @JsonProperty("catchDifficulty") final int catchDifficulty,
             @NotNull @JsonProperty("view") final String view) {
        this.view = view;
        this.title = title;
        this.coinYield = coinYield;
        this.catchDifficulty = catchDifficulty;
    }

    public static @Nullable CellType getCellTypeByTitle(final @NotNull String title) {
        for (final CellType cellType : CellType.values()) {
            if (cellType.title.equals(title)) {
                return cellType;
            }
        }
        return null;
    }

    @Contract(pure = true)
    public @NotNull String getTitle() {
        return title;
    }

    @Contract(pure = true)
    public int getCoinYield() {
        return coinYield;
    }

    @Contract(pure = true)
    public @NotNull String getView() {
        return view;
    }

    @Contract(pure = true)
    public int getCatchDifficulty() {
        return catchDifficulty;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "CellType{" +
                "title='" + title + '\'' +
                ", view='" + view + '\'' +
                ", catchDifficulty=" + catchDifficulty +
                ", coinYield=" + coinYield +
                '}';
    }
}
