package io.neolab.internship.coins.utils;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomGenerator {
    private static final Random random = new Random();

    /**
     * Выбрать элемент из списка (подбросить монетку)
     *
     * @param list - список доступных на выбор клеток
     * @return выбранный элемент
     */
    public static <T> T chooseItemFromList(final List<T> list) {
        final int numberOfCell = chooseNumber(list.size()); // номер выбранной клетки из списка
        return list.get(numberOfCell);
    }

    /**
     * Выбрать число (подбросить монетку)
     *
     * @param bound - граница подходящего числа
     * @return выбранное число
     */
    public static int chooseNumber(final int bound) {
        return random.nextInt(bound);
    }
}
