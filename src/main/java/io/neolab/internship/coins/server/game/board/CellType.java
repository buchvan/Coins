package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public enum CellType implements Serializable {
    LAND("LAND", 1, 2, "L"),
    MUSHROOM("MUSHROOM", 1, 2, "m"),
    MOUNTAIN("MOUNTAIN", 1, 3, "M"),
    WATER("WATER", 1, 1, "W");

    private final String title;
    private final String view;
    private final int catchDifficulty; // Сложность захвата клетки
    private final int coinYield; // Число монет, которое приносит клетка

    @JsonCreator
    CellType(@JsonProperty("title") final String title,
             @JsonProperty("coinYield") final int coinYield,
             @JsonProperty("catchDifficulty") final int catchDifficulty,
             @JsonProperty("view") final String view) {
        this.view = view;
        this.title = title;
        this.coinYield = coinYield;
        this.catchDifficulty = catchDifficulty;
    }

    public static CellType getCellTypeByTitle(final String title) {
        for (final CellType cellType : CellType.values()) {
            if (cellType.title.equals(title)) {
                return cellType;
            }
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public int getCoinYield() {
        return coinYield;
    }

    public String getView() {
        return view;
    }

    public int getCatchDifficulty() {
        return catchDifficulty;
    }

    @Override
    public String toString() {
        return "CellType{" +
                "title='" + title + '\'' +
                ", view='" + view + '\'' +
                ", catchDifficulty=" + catchDifficulty +
                ", coinYield=" + coinYield +
                '}';
    }
}
