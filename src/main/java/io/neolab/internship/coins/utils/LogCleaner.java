package io.neolab.internship.coins.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;

public class LogCleaner {
    static final @NotNull String LOGBACK = "./src/main/resources/logback.xml";
    static final @NotNull String PROPERTY_NAME = "LOG_DIRECTORY";
    public static final int LOGS_BORDER = 20;

    /**
     * Загружает название директории с логами из logback
     *
     * @return значение свойства propertyName
     * @throws IOException при ошибке чтения из файла logback
     */
    public static @NotNull String loadLogDirectory() throws IOException {
        final String searchingLine = "    <property name=\"" + PROPERTY_NAME + "\" ";
        String line;
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(LOGBACK))) {
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
        clean(loadLogDirectory());
    }

    /**
     * Удаляет все логи из директории logDirectory при условии, что их число там больше, чем LOGS_MAX_COUNT
     *
     * @param logDirectory - директория, из которой необходимо удалить логи
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void clean(final String logDirectory) {
        final File logDir = new File(logDirectory);
        if (logDir.exists()) {
            final File[] logs = logDir.listFiles();
            if (logs != null && logs.length > LOGS_BORDER) {
                Arrays.stream(logs).forEach(File::delete);
            }
        }
    }

    /**
     * Удаляет все логи из директории logDirectory по подстроке имени
     *
     * @param subName - подстрока имени
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanBySubName(final String subName) throws IOException {
        final File logDir = new File(loadLogDirectory());
        if (logDir.exists()) {
            final File[] logs = logDir.listFiles(file -> file.getName().contains(subName));
            if (logs != null) {
                Arrays.stream(logs).forEach(File::delete);
            }
        }
    }
}
