package io.neolab.internship.coins.server.game.board;

import org.apache.commons.collections4.BidiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBoard {

    /**
     * @return число строк
     */
    int getSizeX();

    /**
     * @return число столбцов
     */
    int getSizeY();

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
