package io.neolab.internship.coins.utils;


import io.neolab.internship.coins.TestUtils;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class LogCleanerTest extends TestUtils {

    @Test
    public void testClean() throws IOException {
        final String testLogDirectory = LogCleaner.loadLogDirectory() + "/logCleaner";
        int i = 0;
        final File testFile = new File(testLogDirectory);
        assertTrue(testFile.mkdir());
        testFile.deleteOnExit();
        while (i < LogCleaner.LOGS_BORDER + 1) {
            try (final BufferedWriter bufferedWriter = new BufferedWriter(
                    new FileWriter(testLogDirectory + "/" + i))) {
                bufferedWriter.write("" + i);
            }
            i++;
        }
        final File dir = new File(testLogDirectory);
        assertTrue(dir.listFiles().length > LogCleaner.LOGS_BORDER);
        LogCleaner.clean(testLogDirectory);
        assertEquals(0, dir.listFiles().length);
        assertTrue(testFile.delete());
        assertFalse(testFile.exists());
    }
}
