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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class Board implements IBoard, Serializable {

    @JsonProperty
    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionKeyDeserializer.class, using = PositionToCellBidiMapDeserializer.class)
    private final @NotNull BidiMap<Position, Cell> positionToCellMap;

    @JsonProperty
    private final @NotNull List<Cell> edgeCells;

    @JsonProperty
    @JsonSerialize(keyUsing = CellSerializer.class)
    @JsonDeserialize(keyUsing = CellKeyDeserializer.class)
    private final @NotNull Map<Cell, List<Cell>> cellToNeighboringCells;

    /**
     * @param sizeX             - число строк
     * @param sizeY             - число столбцов
     * @param positionToCellMap - позиция в клетку
     * @return список крайних клеток
     */
    private static List<Cell> findEdgeCells(final int sizeX, final int sizeY,
                                            final @NotNull BidiMap<Position, Cell> positionToCellMap) {
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
    public Board(@NotNull @JsonProperty("positionToCellMap") final BidiMap<Position, Cell> positionToCellMap,
                 @NotNull @JsonProperty("edgeCells") final List<Cell> edgeCells,
                 @NotNull @JsonProperty("cellToNeighboringCells") final Map<Cell, List<Cell>> cellToNeighboringCells) {
        this.positionToCellMap = positionToCellMap;
        this.edgeCells = edgeCells;
        this.cellToNeighboringCells = cellToNeighboringCells;
    }

    public Board() {
        this(3, 4, new DualHashBidiMap<>());
    }

    @Contract(pure = true)
    @JsonIgnore
    @Override
    public @NotNull IBoard getCopy() {
        final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();
        this.positionToCellMap.forEach((position, cell) -> positionToCellMap.put(position, cell.getCopy()));

        final List<Cell> edgeCells = new LinkedList<>();
        this.edgeCells.forEach(cell -> edgeCells.add(positionToCellMap.get(getPositionByCell(cell))));

        final Map<Cell, List<Cell>> cellToNeighboringCells = new HashMap<>(this.cellToNeighboringCells.size());
        this.cellToNeighboringCells.forEach((cell, cells) -> {
            final List<Cell> neighboringCells = new LinkedList<>();
            cells.forEach(cell1 ->
                    neighboringCells.add(positionToCellMap.get(getPositionByCell(cell1))));
            cellToNeighboringCells.put(positionToCellMap.get(getPositionByCell(cell)), neighboringCells);
        });

        return new Board(positionToCellMap, edgeCells, cellToNeighboringCells);
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
