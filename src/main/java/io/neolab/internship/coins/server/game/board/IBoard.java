package io.neolab.internship.coins.server.game.board;

import org.apache.commons.collections4.BidiMap;

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
    BidiMap<Position, Cell> getPositionToCellMap();

    /**
     * Взять клетку по позиции, на которой она расположена на борде
     *
     * @param position - позиция, по которой нужно взять клетку
     * @return клетку, расположенную на позиции position
     */
    Cell getCellByPosition(final Position position);

    /**
     * Взять клетку по позиции, на которой она расположена на борде
     *
     * @param x - позиция по x, по которой нужно взять клетку
     * @param y - позиция по y, по которой нужно взять клетку
     * @return клетку, расположенную на позиции position
     */
    Cell getCellByPosition(final int x, final int y);

    /**
     * Взять позицию клетки
     *
     * @param cell - клетка, чью позиция мы спрашиваем у борды
     * @return позицию клетки cell
     */
    Position getPositionByCell(final Cell cell);

    /* Метод взятия всех крайних клеток борды */
    List<Cell> getEdgeCells();
}
