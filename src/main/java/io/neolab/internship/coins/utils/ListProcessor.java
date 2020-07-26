package io.neolab.internship.coins.utils;

import io.neolab.internship.coins.exceptions.UtilsException;
import io.neolab.internship.coins.exceptions.UtilsErrorCode;
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
    public static <T> void removeFirstN(final int N, final @NotNull List<T> list) throws UtilsException {
        if (N < 0) {
            throw new UtilsException(UtilsErrorCode.INDEX_OUT_OF_BOUNDS);
        }
        int i = 0;
        while (i < N && i < list.size()) {
            list.remove(0);
            i++;
        }
    }
}
