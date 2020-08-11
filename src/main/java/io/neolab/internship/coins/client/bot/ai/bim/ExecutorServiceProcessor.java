package io.neolab.internship.coins.client.bot.ai.bim;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceProcessor {
    private static final long TIMEOUT_MILLIS = 5000; //Note: если делать параллельный прогон нескольких игр,
    // то стоит данное значение намного увеличить

    /**
     * Выполнить ExecutorService
     *
     * @param executorService - очевидно, ExecutorService
     */
    static void executeExecutorService(final @NotNull ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
