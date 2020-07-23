package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.serialization.deserialize.CellKeyDeserializer;
import io.neolab.internship.coins.common.serialization.deserialize.PositionKeyDeserializer;
import io.neolab.internship.coins.common.serialization.deserialize.PositionToCellBidiMapDeserializer;
import io.neolab.internship.coins.common.serialization.serialize.CellSerializer;
import io.neolab.internship.coins.common.serialization.serialize.PositionSerializer;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.Serializable;
import java.util.*;

public class Board implements IBoard, Serializable {

    @JsonProperty
    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionKeyDeserializer.class, using = PositionToCellBidiMapDeserializer.class)
    private final BidiMap<Position, Cell> positionToCellMap;

    @JsonProperty
    private final List<Cell> edgeCells;

    @JsonProperty
    @JsonSerialize(keyUsing = CellSerializer.class)
    @JsonDeserialize(keyUsing = CellKeyDeserializer.class)
    private final Map<Cell, List<Cell>> cellToNeighboringCells;

    /**
     * @param sizeX             - число строк
     * @param sizeY             - число столбцов
     * @param positionToCellMap - позиция в клетку
     * @return список крайних клеток
     */
    private static List<Cell> findEdgeCells(final int sizeX, final int sizeY,
                                            final BidiMap<Position, Cell> positionToCellMap) {
        final List<Cell> edgeCells = new LinkedList<>();
        int strIndex;
        int colIndex = 0;
        while (colIndex < sizeY) { // обход по верхней границе борды
            edgeCells.add(positionToCellMap.get(new Position(0, colIndex)));
            colIndex++;
        }
        strIndex = 1;
        colIndex--; // colIndex = sizeY;
        while (strIndex < sizeX) { // обход по правой границе борды
            edgeCells.add(positionToCellMap.get(new Position(strIndex, colIndex)));
            strIndex++;
        }
        strIndex--; // strIndex = sizeX;
        colIndex--; // colIndex = sizeY - 1;
        while (colIndex >= 0) { // обход по нижней границе борды
            edgeCells.add(positionToCellMap.get(new Position(strIndex, colIndex)));
            colIndex--;
        }
        strIndex--; // strIndex = sizeX - 1;
        colIndex++; // strIndex = 0;
        while (strIndex > 0) { // обход по левой границе борды
            edgeCells.add(positionToCellMap.get(new Position(strIndex, colIndex)));
            strIndex--;
        }
        return edgeCells;
    }

    public Board(final int sizeX, final int sizeY, final BidiMap<Position, Cell> positionToCellMap) {
        this.positionToCellMap = new DualHashBidiMap<>();
        positionToCellMap.forEach(this.positionToCellMap::put);
        edgeCells = findEdgeCells(sizeX, sizeY, positionToCellMap);
        this.cellToNeighboringCells = new HashMap<>();
    }

    @JsonCreator
    public Board(@JsonProperty("positionToCellMap") final BidiMap<Position, Cell> positionToCellMap,
                 @JsonProperty("edgeCells") final List<Cell> edgeCells,
                 @JsonProperty("cellToNeighboringCells") final Map<Cell, List<Cell>> cellToNeighboringCells) {
        this.positionToCellMap = positionToCellMap;
        this.edgeCells = edgeCells;
        this.cellToNeighboringCells = cellToNeighboringCells;
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
    public BidiMap<Position, Cell> getPositionToCellMap() {
        return positionToCellMap;
    }

    @Override
    public List<Cell> getEdgeCells() {
        return edgeCells;
    }

    @Override
    public List<Cell> getNeighboringCells(final Cell cell) {
        return cellToNeighboringCells.get(cell);
    }

    @Override
    public void putNeighboringCells(final Cell cell, final List<Cell> neighboringCells) {
        cellToNeighboringCells.put(cell, neighboringCells);
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
