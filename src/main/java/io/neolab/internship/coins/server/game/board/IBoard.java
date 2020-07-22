package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.collections4.BidiMap;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Board.class, name = "Board"),
})
public interface IBoard {

    int getSizeX();

    int getSizeY();

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
