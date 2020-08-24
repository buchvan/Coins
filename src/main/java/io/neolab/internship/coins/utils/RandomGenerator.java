package io.neolab.internship.coins.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomGenerator {
    private static final @NotNull Random random = new Random();

    /**
     * Выбрать элемент из списка (подбросить монетку)
     *
     * @param list - список доступных на выбор клеток
     * @return выбранный элемент
     */
    public static <T> T chooseItemFromList(final @NotNull List<T> list) {
        final int numberOfCell = chooseNumber(list.size()); // номер выбранной клетки из списка
        return list.get(numberOfCell);
    }

    /**
     * Выбрать элемент из множества (подбросить монетку)
     *
     * @param set - множество доступных на выбор клеток
     * @return выбранный элемент
     */
    public static <T> T chooseItemFromSet(final @NotNull Set<T> set) {
        final int numberOfCell = chooseNumber(set.size()); // номер выбранной клетки из множества
        int i = 0;
        T result = null;
        for (final T t : set) {
            if (i == numberOfCell) {
                result = t;
                break;
            }
            i++;
        }
        return result;
    }

    /**
     * Выбрать число (подбросить монетку)
     *
     * @param bound - граница подходящего числа
     * @return выбранное число
     */
    public static synchronized int chooseNumber(final int bound) {
        return random.nextInt(bound);
    }

    /**
     * Подбрасываем монетку
     *
     * @return true - если 1, false - если 0
     */
    public static boolean isYes() {
        return chooseNumber(2) == 1;
    }
}
