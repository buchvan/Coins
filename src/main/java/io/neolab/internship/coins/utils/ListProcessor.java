package io.neolab.internship.coins.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListProcessor {

    /**
     * Удаление из списка list первых N элементов. Если N превышает размер списка, то список очищается
     *
     * @param N    - целое число
     * @param list - произвольный список
     * @param <T>  - любой параметр
     */
    public static <T> void removeFirstN(final int N, final @NotNull List<T> list) {
        if (N < 0) {
            throw new IndexOutOfBoundsException(); // TODO: Своё исключение?
        }
        int i = 0;
        while (i < N && i < list.size()) {
            list.remove(0);
            i++;
        }
    }
}
