package io.neolab.internship.coins.utils;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogCleanerTest {
    @Test
    public void testClean() throws IOException {
        final String logDirectory = "logs";
        final int logsMaxCount = 10;
        int i = 0;
        while (i < logsMaxCount) {
            try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logDirectory + "/" + i))) {
                bufferedWriter.write("" + i);
            }
            i++;
        }
        final File dir = new File(logDirectory);
        assertTrue(dir.listFiles().length > logsMaxCount);
        LogCleaner.clean();
        assertEquals(0, dir.listFiles().length);
    }
}
