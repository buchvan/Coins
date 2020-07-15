package io.neolab.internship.coins.utils;

import java.io.*;
import java.util.Arrays;

public class LogCleaner {
    private static final String logback = "src/main/resources/logback.xml";
    private static final String propertyName = "LOG_DIRECTORY";
    private static final int LOGS_MAX_COUNT = 10;

    /**
     * Загружает название директории с логами из logback
     *
     * @return значение свойства propertyName
     * @throws IOException при ошибке чтения из файла logback
     */
    private static String loadLogDirectory() throws IOException {
        final String searchingLine = "    <property name=\"" + propertyName + "\" ";
        String line;
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(logback))) {
            do {
                line = bufferedReader.readLine();
            } while (!line.startsWith(searchingLine));
        }
        return line.split("\"")[3];
    }

    /**
     * Удаляет все логи из директории LOG_DIRECTORY при условии, что их число там больше, чем LOGS_MAX_COUNT
     *
     * @throws IOException при ошибке чтения из файла logback
     */
    public static void clean() throws IOException {
        final String LOG_DIRECTORY = loadLogDirectory();
        final File logDir = new File(LOG_DIRECTORY);
        if (logDir.exists()) {
            final File[] logs = logDir.listFiles();
            if (logs != null && logs.length > LOGS_MAX_COUNT) {
                Arrays.stream(logs).forEach(File::delete);
            }
        }
    }
}
