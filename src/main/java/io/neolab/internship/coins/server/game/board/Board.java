package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.deserialize.CellKeyDeserializer;
import io.neolab.internship.coins.common.deserialize.PositionDeserializer;
import io.neolab.internship.coins.common.deserialize.PositionToCellBidiMapDeserializer;
import io.neolab.internship.coins.common.serialize.CellSerializer;
import io.neolab.internship.coins.common.serialize.PositionSerializer;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

@JsonDeserialize
public class Board implements IBoard, Serializable {
    private final int sizeX;
    private final int sizeY;

    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionDeserializer.class, using = PositionToCellBidiMapDeserializer.class)
    private final @NotNull BidiMap<Position, Cell> positionToCellMap;

    private final @NotNull List<Cell> edgeCells;

    @JsonSerialize(keyUsing = CellSerializer.class)
    @JsonDeserialize(keyUsing = CellKeyDeserializer.class)
    private final @NotNull Map<Cell, List<Cell>> cellToNeighboringCells;

    public Board(final int sizeX, final int sizeY, final @NotNull BidiMap<Position, Cell> positionToCellMap) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.positionToCellMap = new DualHashBidiMap<>();
        positionToCellMap.forEach(this.positionToCellMap::put);
        edgeCells = new LinkedList<>();
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

        this.cellToNeighboringCells = new HashMap<>();
    }

    @JsonCreator
    public Board(@JsonProperty("sizeX") final int sizeX,
                 @JsonProperty("sizeY") final int sizeY,
                 @NotNull @JsonProperty("positionToCellMap") final BidiMap<Position, Cell> positionToCellMap,
                 @NotNull @JsonProperty("edgeCells") final List<Cell> edgeCells,
                 @NotNull @JsonProperty("cellToNeighboringCells") final Map<Cell, List<Cell>> cellToNeighboringCells) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.positionToCellMap = new DualHashBidiMap<>();
        positionToCellMap.forEach(this.positionToCellMap::put);
        this.edgeCells = edgeCells;
        this.cellToNeighboringCells = cellToNeighboringCells;
    }

    public Board() {
        this(3, 4, new DualHashBidiMap<>());
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
    public @NotNull BidiMap<Position, Cell> getPositionToCellMap() {
        return positionToCellMap;
    }

    @Override
    public @NotNull List<Cell> getEdgeCells() {
        return edgeCells;
    }

    @Override
    public @Nullable List<Cell> getNeighboringCells(final @NotNull Cell cell) {
        return cellToNeighboringCells.get(cell);
    }

    @Override
    public void putNeighboringCells(final @NotNull Cell cell, final @NotNull List<Cell> neighboringCells) {
        cellToNeighboringCells.put(cell, neighboringCells);
    }

    @Override
    public @Nullable Cell getCellByPosition(final @NotNull Position position) {
        return getPositionToCellMap().get(position);
    }

    @Override
    public @Nullable Cell getCellByPosition(final int x, final int y) {
        return getCellByPosition(new Position(x, y));
    }

    @Override
    public @NotNull Position getPositionByCell(final @NotNull Cell cell) {
        return getPositionToCellMap().getKey(cell);
    }

    @Contract(value = "null -> false", pure = true)
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
