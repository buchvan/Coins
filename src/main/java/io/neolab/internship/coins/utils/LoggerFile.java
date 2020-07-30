package io.neolab.internship.coins.utils;

import io.neolab.internship.coins.server.service.GameLogger;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerFile implements AutoCloseable {
    private final String logFileName;

    public LoggerFile(final String prefix) {
        final SimpleDateFormat formatForDateNow = new SimpleDateFormat("E-yyyy-MM-dd-hh-mm-ss-S");

        /* генерируем имя файла-лога (prefix__E-yyyy-MM-dd-hh-mm-ss-S) */
        logFileName = prefix + "__" +
                formatForDateNow.format(new Date())
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
