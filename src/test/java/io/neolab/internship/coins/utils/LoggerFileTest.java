package io.neolab.internship.coins.utils;

import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LoggerFileTest {
    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testCreatingLoggerFileNull() {
        new LoggerFile(null);
    }

    @Test
    public void testLoggerFile() {
        final String logFileName = "logFileName";
        final String prefix = "test";
        try (final LoggerFile ignored = new LoggerFile(prefix)) {
            assertTrue(MDC.get(logFileName) != null && MDC.get(logFileName).contains(prefix));
        }
        assertNull(MDC.get("logFileName"));
    }
}
