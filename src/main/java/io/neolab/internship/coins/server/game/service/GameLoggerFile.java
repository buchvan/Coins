package io.neolab.internship.coins.server.game.service;

import org.slf4j.MDC;

import java.util.Date;

public class GameLoggerFile implements AutoCloseable {
    private final String logFileName;

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
