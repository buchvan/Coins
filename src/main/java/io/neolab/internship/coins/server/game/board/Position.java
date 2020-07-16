package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Класс позиция, пара целых чисел
 */
//@JsonSerialize(using = PositionSerializer.class)
public class Position implements Serializable {
    private final int x;
    private final int y;

    /**
     * Взять все соседние позиции,
     * т. е. те позиции, в которые можно попасть за один шаг (по горизонтали, по вертикали и по диагонали)
     *
     * @param position - позиция, чьих соседей мы хотим узнать
     * @return список соседних с position позиций
     */
    public static List<Position> getAllNeighboringPositions(final Position position) {
        final List<Position> neighboringPositions = new LinkedList<>();
        int strIndex = -1;
        int colIndex;

        /* В целом это обход всех позиций единичного квадрата с центром в position
        без добавления самого центра position. Делаем проход по строчкам (слева направо) сверху вниз.
        То есть, сначала просматриваем верхнюю строку слева направо, потом среднюю так же и так же с третьей */
        while (strIndex <= 1) {
            colIndex = -1;
            while (colIndex <= 1) {
                if (strIndex != 0 || colIndex != 0) { // если мы сейчас не попали в position
                    neighboringPositions.add(new Position(position.getX() + strIndex, position.getY() + colIndex));
                }
                colIndex++;
            }
            strIndex++;
        }
        return neighboringPositions;
    }

    public Position() {
        this(0, 0);
    }

    public Position(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Position position = (Position) o;
        return x == position.x &&
                y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
