package io.neolab.internship.coins.utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerFile implements AutoCloseable {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(LoggerFile.class);

    private final @NotNull String logFileName;

    public LoggerFile(final @NotNull String prefix) {
        final SimpleDateFormat formatForDateNow = new SimpleDateFormat("E-yyyy-MM-dd-hh-mm-ss-S");

        /* генерируем имя файла-лога (prefix__E-yyyy-MM-dd-hh-mm-ss-S) */
        logFileName = prefix + "__" +
                formatForDateNow.format(new Date())
                        .replaceAll(":", "-");
        MDC.put("logFileName", logFileName); // в logback.xml по этому ключу берётся имя файла для лога
        printLogFileLog(logFileName);
    }

    /**
     * Вывод лога о файле, куда ведётся логгирование
     *
     * @param logFileName - имя файла-лога
     */
    private static synchronized void printLogFileLog(final @NotNull String logFileName) {
        LOGGER.debug("* Logging in file {} *", logFileName);
    }

    @Override
    public void close() {
        printLogFileLog(logFileName);
        MDC.remove("logFileName");
    }
}
