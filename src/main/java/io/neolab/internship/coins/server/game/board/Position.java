package io.neolab.internship.coins.server.game.board;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Класс позиция, пара целых чисел
 */
public class Position {
    private final int x;
    private final int y;

    /**
     * Взять все соседние позиции
     *
     * @param position - позиция, чьих соседей мы хотим узнать
     * @return список соседних с position позиций
     */
    public static List<Position> getAllNeighboringPositions(final Position position) {
        final List<Position> neighboringPositions = new LinkedList<>();
        int strIndex;
        int colIndex = -1;
        /* в целом это обход всех позиций единичного квадрата с центром в position
        без обработки самого центра position */
        while (colIndex <= 1) { // в общем это проход по строчкам (слева направо) снизу вверх.
            // То есть, сначала просматриваем нижнюю строчку слева направо, потом среднюю слева направо,
            // и в конце верхнюю также - слева направо
            strIndex = -2;
            while (strIndex <= 1) {
                strIndex++;
                if (strIndex == 0 && colIndex == 0) { // если мы сейчас в центре единичного квадрата с центом в position
                    continue;
                }
                neighboringPositions.add(new Position(position.getX() + strIndex, position.getY() + colIndex));
            }
            colIndex++;
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
