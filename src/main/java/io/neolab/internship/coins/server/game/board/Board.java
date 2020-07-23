package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.serialization.deserialize.PositionKeyDeserializer;
import io.neolab.internship.coins.common.serialization.deserialize.PositionToCellBidiMapDeserializer;
import io.neolab.internship.coins.common.serialization.serialize.PositionSerializer;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Board implements IBoard, Serializable {
    @JsonProperty
    private final int sizeX;

    @JsonProperty
    private final int sizeY;

    @JsonProperty
    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionKeyDeserializer.class, using = PositionToCellBidiMapDeserializer.class)
    private final BidiMap<Position, Cell> positionToCellMap;

    @JsonProperty
    private final List<Cell> edgeCells;

    private static List<Cell> getEdgeCells(final int sizeX, final int sizeY,
                                           final BidiMap<Position, Cell> positionToCellMap) {
        final List<Cell> edgeCells = new LinkedList<>();
        int strIndex;
        int colIndex = 0;
        while (colIndex < sizeY) { // обход по верхней границе борды
            edgeCells.add(positionToCellMap.get(new Position(0, colIndex)));
            colIndex++;
        }
        strIndex = 1;
        colIndex--; // colIndex = BOARD_SIZE_Y;
        while (strIndex < sizeX) { // обход по правой границе борды
            edgeCells.add(positionToCellMap.get(new Position(strIndex, colIndex)));
            strIndex++;
        }
        strIndex--; // strIndex = BOARD_SIZE_X;
        colIndex--; // colIndex = BOARD_SIZE_Y - 1;
        while (colIndex >= 0) { // обход по нижней границе борды
            edgeCells.add(positionToCellMap.get(new Position(strIndex, colIndex)));
            colIndex--;
        }
        strIndex--; // strIndex = BOARD_SIZE_X - 1;
        colIndex++; // strIndex = 0;
        while (strIndex > 0) { // обход по левой границе борды
            edgeCells.add(positionToCellMap.get(new Position(strIndex, colIndex)));
            strIndex--;
        }
        return edgeCells;
    }

    public Board(final int sizeX, final int sizeY, final BidiMap<Position, Cell> positionToCellMap) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.positionToCellMap = new DualHashBidiMap<>();
        positionToCellMap.forEach(this.positionToCellMap::put);
        edgeCells = getEdgeCells(sizeX, sizeY, positionToCellMap);
    }

    @JsonCreator
    public Board(@JsonProperty("sizeX") final int sizeX,
                 @JsonProperty("sizeY") final int sizeY,
                 @JsonProperty("positionToCellMap") final BidiMap<Position, Cell> positionToCellMap,
                 @JsonProperty("edgeCells") final List<Cell> edgeCells) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.positionToCellMap = positionToCellMap;
        this.edgeCells = edgeCells;
    }

    public Board() {
        this(3, 4, new DualHashBidiMap<>());
    }

    @Override
    public IBoard getCopy() {
        final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();
        this.positionToCellMap.forEach((position, cell) -> positionToCellMap.put(position.getCopy(), cell.getCopy()));
        return new Board(this.sizeX, this.sizeY, positionToCellMap,
                getEdgeCells(this.sizeX, this.sizeY, positionToCellMap));
    }

    @Override
    public int getSizeX() {
        return sizeX;
    }

    @Override
    public int getSizeY() {
        return sizeY;
    }

    @Override
    public BidiMap<Position, Cell> getPositionToCellMap() {
        return positionToCellMap;
    }

    @Override
    public List<Cell> getEdgeCells() {
        return edgeCells;
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
