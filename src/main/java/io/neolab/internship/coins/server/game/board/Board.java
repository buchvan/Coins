package io.neolab.internship.coins.server.game.board;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Objects;

public class Board implements IBoard {
    private final BidiMap<Position, Cell> positionToCellMap;

    public Board(final BidiMap<Position, Cell> positionToCellMap) {
        this.positionToCellMap = new DualHashBidiMap<>();
        positionToCellMap.forEach(this.positionToCellMap::put);
    }

    public Board() {
        this(new DualHashBidiMap<>());
    }

    @Override
    public BidiMap<Position, Cell> getPositionToCellMap() {
        return positionToCellMap;
    }

    @Override
    public Cell getCellByPosition(final Position position) {
        return getPositionToCellMap().get(position);
    }

    @Override
    public Cell getCellByPosition(final int x, final int y) {
        return getCellByPosition(new Position(x, y));
    }

    @Override
    public Position getPositionByCell(final Cell cell) {
        return getPositionToCellMap().getKey(cell);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        final Board board = (Board) o;
        return Objects.equals(getPositionToCellMap(), board.getPositionToCellMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPositionToCellMap());
    }

    @Override
    public String toString() {
        return "Board{" +
                "positionToCellMap=" + positionToCellMap +
                '}';
    }
}
