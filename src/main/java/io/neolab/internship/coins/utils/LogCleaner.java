package io.neolab.internship.coins.utils;

import java.io.*;
import java.util.Arrays;

public class LogCleaner {
    private static final int LOGS_MAX_COUNT = 10;

    public static void clean() throws IOException {
        final String logback = "src/main/resources/logback.xml";
        String line;
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(logback))) {
            do {
                line = bufferedReader.readLine();
            } while (!line.startsWith("    <property name=\"LOG_DIRECTORY\" "));
        }


        // get the property value and print it out
        final String LOG_DIRECTORY = line.split("\"")[3];

        final File logDir = new File(LOG_DIRECTORY);
        if (logDir.exists()) {
            final File[] logs = logDir.listFiles();
            if (logs != null && logs.length >= LOGS_MAX_COUNT) {
                Arrays.stream(logs).forEach(File::delete);
            }
        }
    }
}