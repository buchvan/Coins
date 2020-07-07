package io.neolab.internship.coins.server.game.board;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Objects;

public class Board {
    private BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();

    public Board(BidiMap<Position, Cell> positionToCellMap) {
        this.positionToCellMap = positionToCellMap;
    }

    public Board() {
    }

    public BidiMap<Position, Cell> getPositionToCellMap() {
        return positionToCellMap;
    }

    public void setPositionToCellMap(BidiMap<Position, Cell> positionToCellMap) {
        this.positionToCellMap = positionToCellMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        Board board = (Board) o;
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
