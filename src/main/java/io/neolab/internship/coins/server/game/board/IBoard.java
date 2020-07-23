package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.collections4.BidiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Board.class, name = "Board"),
})
public interface IBoard {
    /**
     * @return копию данного объекта
     */
    @NotNull IBoard getCopy();

    /**
     * @return отображение позиций в клетки
     */
    @NotNull BidiMap<Position, Cell> getPositionToCellMap();

    /**
     * Взять клетку по позиции, на которой она расположена на борде
     *
     * @param position - позиция, по которой нужно взять клетку
     * @return клетку, расположенную на позиции position
     */
    @Nullable Cell getCellByPosition(final Position position);

    /**
     * Взять клетку по позиции, на которой она расположена на борде
     *
     * @param x - позиция по x, по которой нужно взять клетку
     * @param y - позиция по y, по которой нужно взять клетку
     * @return клетку, расположенную на позиции position
     */
    @Nullable Cell getCellByPosition(final int x, final int y);

    /**
     * Взять позицию клетки
     *
     * @param cell - клетка, чью позиция мы спрашиваем у борды
     * @return позицию клетки cell
     */
    @NotNull Position getPositionByCell(final @NotNull Cell cell);

    /**
     * @return список крайних клеток борды
     */
    @NotNull List<Cell> getEdgeCells();

    /**
     * @return список крайних клеток борды
     */
    @NotNull List<Cell> getEdgeCells();

    /**
     * @param cell - клетка
     * @return список соседних с cell клеток
     */
    @Nullable List<Cell> getNeighboringCells(final @NotNull Cell cell);

    /**
     * @param cell             - клетка
     * @param neighboringCells - список соседних с cell клеток
     */
    void putNeighboringCells(final @NotNull Cell cell, final @NotNull List<Cell> neighboringCells);
}
