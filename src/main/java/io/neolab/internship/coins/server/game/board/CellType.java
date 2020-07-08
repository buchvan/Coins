package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Unit;

import java.util.ArrayList;
import java.util.List;

public enum CellType {
    LAND("LAND", 1, new ArrayList<>(2)),
    MUSHROOM("MUSHROOM", 1, new ArrayList<>(2)),
    MOUNTAIN("MOUNTAIN", 1, new ArrayList<>(3)),
    WATER("WATER", 1, new ArrayList<>(1));

    private final String title;

    private int coinYield;

    private List<Unit> defaultCatchUnit;

    CellType(final String title) {
        this.title = title;
    }

    CellType(final String title, final int coinYield, final List<Unit> defaultCatchUnit) {
        this.title = title;
        this.coinYield = coinYield;
        this.defaultCatchUnit = defaultCatchUnit;
    }

    public String getTitle() {
        return title;
    }

    public int getCoinYield() {
        return coinYield;
    }

    public List<Unit> getDefaultCatchUnit() {
        return defaultCatchUnit;
    }


    @Override
    public String toString() {
        return "CellType{" +
                "title='" + title + '\'' +
                ", coinYield=" + coinYield +
                ", defaultCatchUnit=" + defaultCatchUnit +
                '}';
    }
}
