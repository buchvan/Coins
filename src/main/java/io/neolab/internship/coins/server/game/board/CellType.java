package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Unit;

import java.util.ArrayList;
import java.util.List;

public enum CellType {
    LAND("LAND", 1, 2, "L"),
    MUSHROOM("MUSHROOM", 1, 2, "m"),
    MOUNTAIN("MOUNTAIN", 1, 3, "M"),
    WATER("WATER", 1, 1, "W");

    private String title;
    private String view;
    private final int catchDifficulty;
    private final int coinYield;


    CellType(final String title, final int coinYield, final int catchDifficulty, final String view) {
        this.view = view;
        this.title = title;
        this.coinYield = coinYield;
        this.catchDifficulty = catchDifficulty;
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
                ", coinYield=" + coinYield +
                ", catchDifficulty=" + catchDifficulty +
                '}';
    }
}
