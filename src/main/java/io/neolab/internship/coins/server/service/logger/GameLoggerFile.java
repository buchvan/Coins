package io.neolab.internship.coins.server.service.logger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.util.Date;

/**
 * Вспомогательный класс логгера, отвечающий за логгирование в файл
 */
public class GameLoggerFile implements AutoCloseable {
    private final @NotNull String logFileName;

    /**
     * Выбор имени файла-лога и настройка логгирования в него осуществляются прямо в конструкторе
     */
    public GameLoggerFile() {

        /* генерируем имя файла-лога (self-play__HH-mm-ss) */
        logFileName = "self-play__" +
                new Date().toString()
                        .split(" ")[3]
                        .replaceAll(":", "-");

        MDC.put("logFileName", logFileName); // в logback.xml по этому ключу берётся имя файла для лога
        GameLogger.printLogFileLog(logFileName);
    }

    @Override
    public void close() {
        GameLogger.printLogFileLog(logFileName);
        MDC.remove("logFileName");
    }
}
