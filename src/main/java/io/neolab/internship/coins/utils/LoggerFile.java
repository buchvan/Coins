package io.neolab.internship.coins.utils;

import io.neolab.internship.coins.server.service.GameLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.util.Date;

public class LoggerFile implements AutoCloseable {
    private final String logFileName;

    public LoggerFile(final @NotNull String prefix) {

        /* генерируем имя файла-лога (${prefix}__HH-mm-ss) */
        logFileName = prefix + "__" +
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
