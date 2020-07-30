package io.neolab.internship.coins.utils;


import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class LogCleanerTest {
    @Test
    public void testClean() throws IOException {
        final String logDirectory = LogCleaner.loadLogDirectory() + "/test";
        int i = 0;
        final File testFile = new File(logDirectory);
        assertTrue(testFile.mkdir());
        testFile.deleteOnExit();
        while (i < LogCleaner.LOGS_BORDER + 1) {
            try (final BufferedWriter bufferedWriter = new BufferedWriter(
                    new FileWriter(logDirectory + "/" + i))) {
                bufferedWriter.write("" + i);
            }
            i++;
        }
        final File dir = new File(logDirectory);
        assertTrue(dir.listFiles().length > LogCleaner.LOGS_BORDER);
        LogCleaner.clean(logDirectory);
        assertEquals(0, dir.listFiles().length);
        assertTrue(testFile.delete());
        assertFalse(testFile.exists());
    }
}
