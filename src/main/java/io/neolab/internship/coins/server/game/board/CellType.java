package io.neolab.internship.coins.server.game.board;

public enum CellType {
    LAND("LAND", 1, 2),
    MUSHROOM("MUSHROOM", 1, 2),
    MOUNTAIN("MOUNTAIN", 1, 3),
    WATER("WATER", 1, 1);

    private final String title;
    private final int coinYield;
    private final int catchDifficulty;

    CellType(final String title) {
        this(title, 1, 0);
    }

    CellType(final String title, final int coinYield, final int catchDifficulty) {
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
