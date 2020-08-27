package io.neolab.internship.coins.ai_vika.bot.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceProcessor {

    /**
     * Корректно завершает работу executorService
     * @param executorService - executor Service
     */
    public static void completeExecutorService(final ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
